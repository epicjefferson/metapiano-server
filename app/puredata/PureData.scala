package com.rumblesan.puredata

import com.rumblesan.network.{ NettyServer, PDConnection, PDMessage, PDChannel}

import play.api.libs.concurrent._
import play.api.Play.current

import akka.actor._

import play.api._
import play.api.mvc._


object PureData {

  val pd = Akka.system.actorOf(Props[PureData])
  val coms = NettyServer

  def startPD(exe:String,
              port:Int,
              patch:String,
              paths:List[String],
              extraArgs:List[String]) = {
    coms.getBootstrap(port, pd)
    pd ! StartPD(exe, port, patch, paths, extraArgs)
  }
}

class PureData() extends Actor {

  val pdProcess:ActorRef = context.actorOf(Props[PDProcess])

  val pdLogger:ActorRef = {
    val logFile = current.configuration.getString("patchwerk.logfile").get
    context.actorOf(Props(new PDLogger(logFile)))
  }

  var running:Boolean = false

  var channel: PDChannel = null

  def receive = {

    case StartPD(exe, port, patch, paths, extraArgs) => {
      if (running) {
        println("Already Running")
      } else {
        pdProcess ! StartPD(exe, port, patch, paths, extraArgs)
        running = true
      }
    }

    case PDMessage(message) => {
      println(message)
    }

    case PDConnection(connection) => {
      channel = connection
    }

    case PDStdOut(line) => {
      pdLogger ! PDStdOut(line)
    }

    case PDStdErr(line) => {
      pdLogger ! PDStdOut(line)
    }

    case PDFinished(result) => {
      running = false
      pdLogger ! PDFinished(result)
    }

  }

}

class PDLogger(logFile: String) extends Actor {

  import scalax.io._

  val log = Resource.fromFile(logFile)

  def receive = {

    case PDStdOut(line) => {
      log.write(line + "\n")(Codec.UTF8)
    }

    case PDStdErr(line) => {
      log.write("Error: " + line + "\n")(Codec.UTF8)
    }

    case PDFinished(result) => {
      log.write("PD finished with status %d\n".format(result))(Codec.UTF8)
    }

  }

}

class PDProcess() extends Actor {

  import scala.sys.process._

  def createArgList(exe:String,
                    port:Int,
                    patch:String,
                    paths:List[String],
                    extraArgs:List[String]) = {
      val basicArgs = List(exe,
                           "-stderr",
                           "-nogui",
                           "-open",
                           patch,
                           "-send",
                           "startup port %d".format(port))

      val fullPaths = paths.foldLeft(List.empty[String])(
        (total, current) => {
          "-path" :: current :: total
        }
      )

      basicArgs ::: fullPaths ::: extraArgs
  }

  def receive = {

    case StartPD(exe, port, patch, paths, extraArgs) => {

      val args = createArgList(exe, port, patch, paths, extraArgs)
      
      println("Running this\n******\n%s\n".format(args.toString))
      val logger = ProcessLogger(
        line => sender ! PDStdOut(line),
        line => sender ! PDStdErr(line)
      )

      val p = Process(args)
      val result = p ! logger

      sender ! PDFinished(result)

    }

  }

}


case class StartPD(executable:String,
                   port:Int,
                   patch:String,
                   paths:List[String],
                   extraArgs:List[String])


case class PDStdOut(line:String)
case class PDStdErr(line:String)
case class PDFinished(result:Int)


package com.rumblesan.puredata

import play.api.libs.concurrent._
import play.api.Play.current

import akka.actor._

import play.api._
import play.api.mvc._

object PureData {

  lazy val pd = Akka.system.actorOf(Props[PureData])

  def startPD(exe:String,
              port:Int,
              patch:String,
              paths:List[String],
              extraArgs:List[String]) = {
    pd ! StartPD(exe, port, patch, paths, extraArgs)
  }
}

class PureData() extends Actor {

  val pdProcess:ActorRef = context.actorOf(Props[PDProcess])

  val pdListener:ActorRef = context.actorOf(Props[PDListener])

  var running:Boolean = false

  def receive = {

    case StartPD(exe, port, patch, paths, extraArgs) => {
      if (running) {
        println("Already Running")
      } else {
        pdProcess ! StartPD(exe, port, patch, paths, extraArgs)
        running = true
      }
    }

    case PDMessage(line) => {
      pdListener ! PDMessage(line)
    }

    case PDError(line) => {
      pdListener ! PDMessage(line)
    }

    case PDFinished(result) => {
      running = false
      pdListener ! PDFinished(result)
    }

  }

}

class PDListener() extends Actor {

  def receive = {

    case PDMessage(line) => {
      println(line)
    }

    case PDError(line) => {
      println("Error: " + line)
    }

    case PDFinished(result) => {
      println("PD finished with status %d".format(result))
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
        line => sender ! PDMessage(line),
        line => sender ! PDError(line)
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


case class PDMessage(line:String)
case class PDError(line:String)
case class PDFinished(line:Int)


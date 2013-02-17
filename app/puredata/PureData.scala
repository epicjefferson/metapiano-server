package com.rumblesan.puredata

import com.rumblesan.network.{PDComs, PDConnection, PDMessage, PDChannel}

import play.api.libs.concurrent._
import play.api.Play.current

import akka.actor._

import play.api._
import play.api.mvc._


/** This is the actor that starts the PD process as well as handling message passing
  * between it and the rest of the app
  */

class PureDataManager(port: Int) extends Actor {

  val pdProcess:ActorRef = context.actorOf(Props[PureData])
  val pdComs = new PDComs(port, self)

  var running: Boolean = false

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

    case PDConnection(connection) => {
      channel = connection
    }

    case SendPDMessage(message) => {
      channel.write(message)
    }

    case PDMessage(message) => {
      println("Message from PD:  %s".format(message))
    }

  }

}

case class SendPDMessage(message: List[String])




trait FileLogger {

  import scalax.io._

  val logFileName: String
  val log = Resource.fromFile(logFileName)

  def writeToLog(output: String) {
    log.write(output + "\n")(Codec.UTF8)
  }

}

trait PrintLogger {

  def writeToLog(output: String) {
    println(output)
  }

}


trait PureDataProcess {

  import scala.sys.process._

  def createPdArgList(exe:String,
                      port:Int,
                      patch:String,
                      paths:List[String],
                      extraArgs:List[String]): List[String] = {
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

  def startPdProcess(args: List[String]) = {
      val logger = ProcessLogger(
        line => pdProcessStdOut(line),
        line => pdProcessStdErr(line)
      )

      val p = Process(args)
      val result = p ! logger
      pdProcessFinished(result)
  }

  def pdProcessStdOut(line: String): Unit

  def pdProcessStdErr(line: String): Unit

  def pdProcessFinished(result: Int): Unit

}


case class StartPD(exe:String, port:Int, patch:String, paths:List[String], extraArgs:List[String])

class PureData() extends Actor with PureDataProcess with PrintLogger {

  def pdProcessStdOut(line: String) {
    writeToLog(line)
  }

  def pdProcessStdErr(line: String) {
    writeToLog(line)
  }

  def pdProcessFinished(result: Int) {
    writeToLog("PD finished with status %d".format(result))
  }

  def receive = {

    case StartPD(exe, port, patch, paths, extraArgs) => {
      val args = createPdArgList(exe, port, patch, paths, extraArgs)
      startPdProcess(args)
    }

  }

}


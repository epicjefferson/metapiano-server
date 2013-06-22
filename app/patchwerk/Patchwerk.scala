package com.rumblesan.patchwerk

import com.rumblesan.scalapd.{ PureDataManager, FileLogger, LogMessage, StartPD, KillPd, SendPDMessage }
import com.rumblesan.scalapd.network.PDMessage

import play.api.libs.concurrent._
import akka.actor._

import play.api.Play.current
import play.api.Logger


object PatchWerk {

  lazy val patchwerk = Akka.system.actorOf(Props[Patchwerk])

  lazy val logFileName = current.configuration.getString("patchwerk.logfile").get

  def startPD() = {
    val pdExe = current.configuration.getString("patchwerk.puredata").get
    val port = current.configuration.getInt("patchwerk.port").get
    val patch = current.configuration.getString("patchwerk.patch").get
    val paths = current.configuration.getString("patchwerk.paths").get.split(",").toList
    val extras = List.empty[String]

    Logger.info("startPD called")

    patchwerk ! StartPD(pdExe, port, patch, paths, extras, Some(patchwerk))
  }

  def stopPD() = {
    patchwerk ! KillPd()
  }

}


class PatchwerkListener(logFileName: String) extends Actor {

  import java.io.{BufferedWriter, FileWriter}

  val fileOut = new BufferedWriter(new FileWriter(logFileName))

  def writeToLog(output: String) {
    fileOut.write(output + "\n")
    fileOut.flush()
  }

  def receive = {

    case LogMessage(line) => {
      writeToLog(line)
    }

  }

}


class Patchwerk extends Actor {

  Logger.info("patchwerk class created")

  lazy val listener = Props(new PatchwerkListener(PatchWerk.logFileName))

  lazy val puredata = Akka.system.actorOf(Props(new PureDataManager(listener)))

  lazy val statemanager = Akka.system.actorOf(Props(new StateManager(puredata)))

  lazy val patchloader = Akka.system.actorOf(Props[PatchLoader])

  def receive = {

    case PDMessage(message) => parsePDMessage(message)

    case message: SendPDMessage => puredata ! message

    case PatchLoad(message) => {
      Logger.info("patch load sent to PD" + message.toString)
      puredata ! SendPDMessage(message)
    }

    case start: StartPD => {
      puredata ! start
      Logger.info("start pd message sent")
    }

    case kill: KillPd => puredata ! kill

  }

  def parsePDMessage(message: List[String]) {

    Logger.info("pd message received")

    message match {
      case "started" :: Nil => {
        Logger.info("PD has started")

        statemanager ! StateChange(1)

      }
      case "patchload" :: Nil => {
        Logger.info("Patch requested")
        patchloader ! PatchRequest()
      }
      case "poemload" :: Nil => {
        Logger.info("wanted to load a poem")
      }
      case other => Logger.info("something else" + other.toString)
    }

  }

}



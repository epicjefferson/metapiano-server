package com.rumblesan.patchwerk

import com.rumblesan.scalapd.{ PureDataManager, FileLogger, LogMessage, StartPD, KillPd, SendPDMessage }
import com.rumblesan.scalapd.network.PDMessage

import play.api.libs.concurrent._
import akka.actor._

import play.api.Play.current


object PatchWerk {

  lazy val patchwerk = Akka.system.actorOf(Props[Patchwerk])

  lazy val logFileName = current.configuration.getString("patchwerk.logfile").get

  def startPD() = {
    val pdExe = current.configuration.getString("patchwerk.puredata").get
    val port = current.configuration.getInt("patchwerk.port").get
    val patch = current.configuration.getString("patchwerk.patch").get
    val paths = current.configuration.getString("patchwerk.paths").get.split(",").toList
    val extras = List.empty[String]

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

  lazy val listener = Props(new PatchwerkListener(PatchWerk.logFileName))

  lazy val puredata = Akka.system.actorOf(Props(new PureDataManager(listener)))

  lazy val statemanager = Akka.system.actorOf(Props(new StateManager(puredata)))

  lazy val patchloader = Akka.system.actorOf(Props[PatchLoader])

  def receive = {

    case PDMessage(message) => parsePDMessage(message)

    case message: SendPDMessage => puredata ! message

    case start: StartPD => puredata ! start

    case kill: KillPd => puredata ! kill

    case start: StartPD => puredata ! start

  }

  def parsePDMessage(message: List[String]) {

    message match {
      case "started" :: Nil => {
        println("PD has started")

        statemanager ! StateChange(1)

      }
      case "patchload" :: Nil => println("wanted to load a patch")
      case "poemload" :: Nil => println("wanted to load a poem")
      case other => println("something else" + other.toString)
    }

  }

}



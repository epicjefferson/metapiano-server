package com.rumblesan.metapiano

import com.rumblesan.scalapd.{ PureDataManager, StartPD, KillPd, PDMessage, LogMessage, SendPDMessage }

import play.api.libs.concurrent._
import akka.actor._

import play.api.Play.current
import play.api.Logger

import scala.collection.JavaConversions._

object MetaPiano {

  lazy val metapiano = Akka.system.actorOf(Props[MetaPiano])

  lazy val logFileName = current.configuration.getString("metapiano.logfile").get

  def start() = {

    val pdExe = current.configuration.getString("metapiano.puredata").get
    val port = current.configuration.getInt("metapiano.port").get

    val masterpatch = current.configuration.getString("metapiano.masterpatch").get

    val masterpatchfolder = current.configuration.getString("metapiano.masterpatchfolder").get
    val patchfolder = current.configuration.getString("metapiano.patchfolder").get
    val poemfolder = current.configuration.getString("metapiano.poemfolder").get
    val extrapaths = current.configuration.getStringList("metapiano.extrapaths").get.toList

    val paths = masterpatchfolder :: patchfolder :: poemfolder :: extrapaths

    val extras = List.empty[String]

    metapiano ! StartPD(pdExe, port, masterpatch, paths.toList, extras, Some(metapiano))
  }

  def stop() = {
    metapiano ! KillPd()
  }

}


class PDListener(logFileName: String) extends Actor {

  import java.io.{BufferedWriter, FileWriter}

  val fileOut = new BufferedWriter(new FileWriter(logFileName))

  def receive = {

    case LogMessage(output) => {
      fileOut.write(output + "\n")
      fileOut.flush()
    }

  }

}


class MetaPiano extends Actor {

  lazy val listenerProps = Props(new PDListener(MetaPiano.logFileName))

  lazy val puredata = Akka.system.actorOf(Props(new PureDataManager(listenerProps)))

  lazy val statemanager = Akka.system.actorOf(Props(new StateManager(self)))

  lazy val patchloader = Akka.system.actorOf(Props[PatchLoader])

  lazy val poemloader = Akka.system.actorOf(Props[PoemLoader])

  def receive = {

    case PDMessage(message) => parsePDMessage(message)

    case message: SendPDMessage => {
      Logger.info("Message sent to PD")
      puredata ! message
    }

    case PatchLoad(message) => {
      Logger.info("Patch load sent to PD: " + message.tail.head.toString)
      puredata ! SendPDMessage(message)
    }

    case PoemLoad(message) => {
      Logger.info("Poem load sent to PD: " + message.tail.head.toString)
      puredata ! SendPDMessage(message)
    }

    case StateMessage(message) => {
      Logger.info("Sending state change message to PD")
      puredata ! SendPDMessage(message)
    }

    case start: StartPD => {
      Logger.info("Start message sent to PD")
      puredata ! start
    }

    case kill: KillPd => {
      Logger.info("Kill message sent to PD")
      puredata ! kill
    }

  }

  def parsePDMessage(message: List[String]) {

    Logger.info("Message received from PD")

    message match {
      case "started" :: Nil => {
        Logger.info("PD has started")
        statemanager ! StartStateMachine()
      }
      case "patchload" :: Nil => {
        Logger.info("Patch requested")
        patchloader ! PatchRequest()
      }
      case "poemload" :: Nil => {
        Logger.info("Poem requested")
        poemloader ! PoemRequest()
      }
      case other => Logger.info("Unknown message: " + other.toString)
    }

  }

}


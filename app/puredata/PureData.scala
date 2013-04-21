package com.rumblesan.patchwerk

import com.rumblesan.scalapd.{ PureDataManager, FileLogger, LogMessage }
import com.rumblesan.scalapd.network.PDMessage

import play.api.libs.concurrent._
import akka.actor._

import play.api.Play.current


object PureData {

  val logFileName = current.configuration.getString("patchwerk.logfile").get
  val listener = Props(new PatchwerkListener(logFileName))
  lazy val manager = Akka.system.actorOf(Props(new PureDataManager(listener)))

  lazy val patchwerk = Akka.system.actorOf(Props[Patchwerk])

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

  def receive = {

    case PDMessage(message) => println(message)

  }

}


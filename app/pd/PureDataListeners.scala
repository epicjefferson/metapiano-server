package com.rumblesan.scalapd

import akka.actor._

case class LogMessage(line: String)


trait FileLogger {

  import java.io.{BufferedWriter, FileWriter}

  val logFileName: String
  val fileOut = new BufferedWriter(new FileWriter(logFileName))

  def writeToLog(output: String) {
    fileOut.write(output + "\n")
  }

}

trait PrintLogger {

  def writeToLog(output: String) {
    println(output)
  }

}

class PureDataListener extends Actor with PrintLogger {

  def receive = {

    case LogMessage(line) => {
      writeToLog(line)
    }

  }

}


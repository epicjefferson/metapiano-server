package com.rumblesan.scalapd.util

import akka.actor.Actor

import scala.sys.process.Process


class SubProcess extends Actor {

  def receive = {

    case SubProcessRun(p) => {
      val result = p.exitValue()
      sender ! SubProcessFinished(result)
    }

  }

}

case class SubProcessRun(p: Process)
case class SubProcessFinished(result: Int)


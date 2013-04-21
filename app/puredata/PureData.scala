package com.rumblesan.patchwerk

import com.rumblesan.scalapd.PureDataManager
import com.rumblesan.scalapd.network.PDMessage

import play.api.libs.concurrent._
import akka.actor._

import play.api.Play.current


object PureData {

  lazy val manager = Akka.system.actorOf(Props[PureDataManager])

  lazy val patchwerk = Akka.system.actorOf(Props[Patchwerk])

}


class Patchwerk extends Actor {

  def receive = {

    case PDMessage(message) => println(message)

  }

}


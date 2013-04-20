package com.rumblesan.patchwerk

import com.rumblesan.scalapd.PureDataManager

import play.api.libs.concurrent._
import akka.actor._

import play.api.Play.current


object PureData {

  lazy val manager = Akka.system.actorOf(Props(new PureDataManager(9000)))

}



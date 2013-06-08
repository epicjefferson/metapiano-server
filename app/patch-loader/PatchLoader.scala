package com.rumblesan.patchwerk

import play.api.libs.concurrent._
import akka.actor._

import play.api.Play.current


class PatchLoader extends Actor {

  def receive = {

    case PatchRequest => sender ! PatchLoad(List("load", "patchname"))

  }

}

case class PatchLoad(message: List[String])
case class PatchRequest()


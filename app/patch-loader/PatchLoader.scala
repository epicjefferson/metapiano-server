package com.rumblesan.metapiano

import play.api.libs.concurrent._
import akka.actor._

import play.api.Play.current

import java.io.File

import scala.util.Random

import play.api.Logger



class PatchLoader extends Actor {

  val patchfolder = new File(
    current.configuration.getString("metapiano.patchfolder").get
  )

  def getPatch: String = Random.shuffle(patchfolder.list.toList).head

  def receive = {

    case PatchRequest() => {
      Logger.info("Loading a patch")
      sender ! PatchLoad(
        List(
          "load",
          "patch",
          getPatch,
          patchfolder.getAbsolutePath()
        )
      )
    }

  }

}

case class PatchLoad(message: List[String])
case class PatchRequest()


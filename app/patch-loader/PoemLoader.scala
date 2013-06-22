package com.rumblesan.patchwerk

import play.api.libs.concurrent._
import akka.actor._

import play.api.Play.current

import java.io.File

import scala.util.Random

import play.api.Logger



class PoemLoader extends Actor {

  val poemfolder = new File(
    current.configuration.getString("patchwerk.poemfolder").get
  )

  def getPoem: String = Random.shuffle(poemfolder.list.toList).head

  def receive = {

    case PoemRequest() => {
      Logger.info("Loading a poem")
      sender ! PoemLoad(
        List(
          "load",
          "poem",
          getPoem
        )
      )
    }

  }

}

case class PoemLoad(message: List[String])
case class PoemRequest()


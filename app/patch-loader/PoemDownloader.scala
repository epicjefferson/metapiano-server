package com.rumblesan.metapiano

import play.api.libs.concurrent._
import akka.actor._

import play.api.Play.current

import scala.concurrent.duration._
import scala.concurrent._
import ExecutionContext.Implicits.global

import play.api.Logger

import play.api.libs.ws.WS

import models.MetaPoem


class PoemRetriever extends Actor {

  var scheduledOpt: Option[Cancellable] = None

  val startDuration = 1 minutes
  val fetchDuration = 30 minutes

  val poemUrl = current.configuration.getString("metapiano.site.poemendpoint").get

  def setup = {

    Logger.info("Retrieving poems")

    scheduledOpt =  Some(
      Akka.system.scheduler.schedule(
        startDuration,
        fetchDuration,
        self,
        RetrievePoems
      )
    )

  }

  def getPoems = {
    WS.url(poemUrl).get().map { response =>
      response.json.validate[List[MetaPoem]].fold(
        invalid = (
          error => Logger.error(error.toString)
        ),
        valid = (
          poems => Logger.info(poems.toString)
        )
      )
    }
  }


  def receive = {

    case StartRetrieving => setup

    case RetrievePoems => getPoems


  }

}


case object StartRetrieving
case object RetrievePoems
case object StopRetrieving


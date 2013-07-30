package com.rumblesan.metapiano

import play.api.libs.concurrent._
import akka.actor._

import play.api.Play.current

import scala.concurrent.duration._
import scala.concurrent._
import ExecutionContext.Implicits.global

import play.api.Logger



class PoemRetriever() extends Actor {

  var scheduledOpt: Option[Cancellable] = None

  val fetchDuration = 30 minutes

  def setup = {

    scheduledOpt =  Some(
      Akka.system.scheduler.schedule(
        fetchDuration
        self,
        RetrievePoems
      )
    )

  }

  def getPoems = {
    println("Retrieving Poems")


  }


  def receive = {

    case StartRetrieving => getPoems

    case RetrievePoems => getPoems


  }

}


case object StartRetrieving
case object RetrievePoems
case object StopRetrieving


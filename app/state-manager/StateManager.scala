package com.rumblesan.metapiano

import play.api.libs.concurrent._
import akka.actor._

import play.api.Play.current

import scala.concurrent.duration._
import scala.concurrent._
import ExecutionContext.Implicits.global


class StateManager(targetActor: ActorRef) extends Actor {

  val target = targetActor

  lazy val states:Map[Long, SystemState] = Map(
    1L -> SystemState(1, "basic", 1, List("statename", "patches"), 2),
    2L -> SystemState(2, "different", 1, List("statename", "poems"), 1)
  )

  var currentState: Long = 0

  var scheduledOpt: Option[Cancellable] = None


  def changeState(id: Long) = {
    println("changing state")
    states.get(id).map(state =>

      if (currentState != state.id) {

        currentState = state.id

        println("changing to state %s".format(id))

        target ! StateMessage(state.message)

        for {
          cancellable <- scheduledOpt
          if (!cancellable.isCancelled)
          unit = cancellable.cancel()
        } yield cancellable

        scheduledOpt =  Some(
          Akka.system.scheduler.scheduleOnce(
            state.durationMins minutes,
            self,
            StateChange(state.nextState)
          )
        )


      }

    )
  }


  def receive = {

    case StateQuery => sender ! CurrentState(states(currentState))

    case StateChange(id) => changeState(id)

  }

}


case class SystemState(id: Long, name: String, durationMins: Int, message: List[String], nextState: Long)


case class StateQuery()
case class CurrentState(state: SystemState)

case class StateChange(id: Long)
case class StateMessage(message: List[String])


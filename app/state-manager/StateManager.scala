package com.rumblesan.metapiano

import play.api.libs.concurrent._
import akka.actor._

import play.api.Play.current

import scala.concurrent.duration._
import scala.concurrent._
import ExecutionContext.Implicits.global

import play.api.Logger

import scala.collection.JavaConversions._



class StateManager(targetActor: ActorRef) extends Actor {

  val target = targetActor

  lazy val states: Map[String, SystemState] = {
    (
      for {
        stateconfig <- current.configuration.getObject("metapiano.statechanger.state")
      } yield for {
        statename <- stateconfig.keySet
        state = stateconfig.toConfig.getConfig(statename)
        duration = state.getInt("duration")
        message = state.getStringList("message").toList
        nextstate = state.getString("nextstate")
      } yield (statename, SystemState(statename, duration, message, nextstate))
    ).map(_.toMap).getOrElse(Map.empty[String, SystemState])
  }

  var currentState: String = ""
  var firstState: String = current.configuration.getString("metapiano.statechanger.firststate").get

  var scheduledOpt: Option[Cancellable] = None


  def changeState(name: String) = {
    println("changing state")

      Logger.info("new state is " + name.toString)
      Logger.info("current state is " + currentState)

    states.get(name).map(state =>

      if (currentState != state.name) {

        currentState = state.name

        println("changing to state %s".format(name))

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

    case StateQuery() => {
      Logger.info("Querying my state")

      val reportedState: CurrentState = CurrentState(
        states.getOrElse(currentState, SystemState("No State", 0, Nil, ""))
      )
      sender ! reportedState
    }

    case StateChange(name) => changeState(name)

    case StartStateMachine() => changeState(firstState)

  }

}


case class SystemState(name: String, durationMins: Int, message: List[String], nextState: String)


case class StateQuery()
case class CurrentState(state: SystemState)

case class StateChange(name: String)
case class StateMessage(message: List[String])
case class StartStateMachine()


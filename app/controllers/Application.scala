package controllers

import play.api._
import play.api.mvc._
import play.api.Play.current

import play.api.libs.concurrent._
import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent._
import ExecutionContext.Implicits.global

import com.rumblesan.scalapd._
import com.rumblesan.metapiano.{ MetaPiano, StateQuery, CurrentState, SystemState }

import play.api.libs.json._

object Application extends Controller {
  
  implicit val timeout = Timeout(10 seconds)

  def index = Action {

    val pdstate = (MetaPiano.metapiano ? StateQuery()).mapTo[Future[CurrentState]]
    Async {
      for {
        f <- pdstate
        s <- f
        result = s match {
          case CurrentState(SystemState(name, _, _, _)) => Ok(views.html.main(name))
          case _ => Ok(views.html.main("Error State"))
        }
      } yield result
    }

  }

}

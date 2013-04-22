package controllers

import play.api._
import play.api.mvc._
import play.api.Play.current

import com.rumblesan.scalapd._
import com.rumblesan.patchwerk.PureData

import play.api.libs.json._

object Application extends Controller {
  
  def index = Action {

    Ok(views.html.main())

  }

  def sendmessage = Action(parse.json) { implicit request =>

    request.body.validate[Map[String,String]].map( data => {
      data.get("message").map(m => {
        PureData.manager ! SendPDMessage(m.split(" ").toList)
        Ok("")
      }).getOrElse(BadRequest("No message in JSON"))
    }).getOrElse(BadRequest("Incorrect Json"))

  }
  
}

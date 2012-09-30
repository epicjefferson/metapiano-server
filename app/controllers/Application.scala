package controllers

import com.rumblesan.puredata.PureData

import play.api._
import play.api.mvc._
import play.api.Play.current

object Application extends Controller {
  
  def index = Action {

    Ok("Yup it's running")

  }
  
}

package controllers

import com.rumblesan.puredata.PureData

import play.api._
import play.api.mvc._
import play.api.Play.current

object Application extends Controller {
  
  def index = Action {

    val pdExe = current.configuration.getString("patchwerk.puredata").get
    val port = current.configuration.getInt("patchwerk.port").get
    val patch = current.configuration.getString("patchwerk.patch").get
    val paths = current.configuration.getString("patchwerk.paths").get.split(",").toList
    val extras = List.empty[String]

    PureData.startPD(pdExe, port, patch, paths, extras)

    Ok("Yup it's running")

  }
  
}

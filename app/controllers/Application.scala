package controllers

import com.rumblesan.puredata.PureData

import play.api._
import play.api.mvc._

object Application extends Controller {
  
  def index = Action {

    val pdExe = "/Applications/Pd-extended.app/Contents/Resources/bin/pd"
    val port = 12345
    val patch = "patches/test.pd"
    val paths = List.empty[String]
    val extras = List.empty[String]

    PureData.startPD(pdExe, port, patch, paths, extras)

    Ok("Yup it's running")

  }
  
}

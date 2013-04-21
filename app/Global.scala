import play.api.GlobalSettings

import com.rumblesan.scalapd.{ StartPD, KillPd }
import play.api.Play.current

import play.api.GlobalSettings

import play.api.Application

import play.api.libs.concurrent._
import akka.actor._

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    val pdExe = current.configuration.getString("patchwerk.puredata").get
    val port = current.configuration.getInt("patchwerk.port").get
    val patch = current.configuration.getString("patchwerk.patch").get
    val paths = current.configuration.getString("patchwerk.paths").get.split(",").toList
    val extras = List.empty[String]

    com.rumblesan.patchwerk.PureData.manager ! StartPD(pdExe, port, patch, paths, extras, Some(com.rumblesan.patchwerk.PureData.patchwerk))
  }

  override def onStop(app: Application) {
    com.rumblesan.patchwerk.PureData.manager ! KillPd()
  }

}

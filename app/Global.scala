import play.api.GlobalSettings

import play.api.Application

import com.rumblesan.patchwerk.PatchWerk

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    PatchWerk.startPD()
  }

  override def onStop(app: Application) {
    PatchWerk.stopPD()
  }

}

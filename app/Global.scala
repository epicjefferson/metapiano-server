import play.api.GlobalSettings

import play.api.Application

import com.rumblesan.metapiano.MetaPiano

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    MetaPiano.start()
  }

  override def onStop(app: Application) {
    MetaPiano.stop()
  }

}

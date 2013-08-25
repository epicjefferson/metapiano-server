import org.squeryl.adapters.{H2Adapter, PostgreSqlAdapter}
import org.squeryl.internals.DatabaseAdapter
import org.squeryl.{Session, SessionFactory}
import play.api.db.DB
import play.api.GlobalSettings

import play.api.Application

import com.rumblesan.metapiano.MetaPiano

object Global extends GlobalSettings {

  override def onStart(app: Application) {

    val conf = app.configuration

    val dbName = conf.getString("application.database").getOrElse("default")

    SessionFactory.concreteFactory = conf.getConfig("db." + dbName).flatMap(
      _.getString("driver")
    ) match {
      case Some("org.h2.Driver") => {
        Some(
          () => getSession(new H2Adapter, dbName, app)
        )
      }
      case Some("org.postgresql.Driver") => {
        Some(
          () => getSession(new PostgreSqlAdapter, dbName, app)
        )
      }
      case _ => sys.error("Database driver must be either org.h2.Driver or org.postgresql.Driver")
    }

    MetaPiano.start()
  }

  override def onStop(app: Application) {
    MetaPiano.stop()
  }

  def getSession(adapter:DatabaseAdapter, dbName: String, app: Application) = {
    Session.create(
      DB.getConnection(dbName)(app),
      adapter
    )
  }

}

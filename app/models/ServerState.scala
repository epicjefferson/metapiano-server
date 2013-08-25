package models

import scala.language.implicitConversions

import DBSchema._

import org.squeryl.KeyedEntity
import org.squeryl.PrimitiveTypeMode._

import play.api.libs.json._
import play.api.libs.functional.syntax._

object ServerState {

  def create(state: ServerState): ServerState = inTransaction {
    serverstate.insert(state)
  }

  def retrieveOne(id: Long): Option[ServerState] = inTransaction {
    from(serverstate)(p =>
      where(p.id === id)
      select(p)
    ).headOption
  }

  def retrieveAll(): List[ServerState] = inTransaction {
    from(serverstate)(select(_)).toList
  }

  def updateFull(state: ServerState): Int = inTransaction {
    update(serverstate)(p =>
      where(p.id === state.id)
      set(p.lastPoem := state.lastPoem)
    )
  }

  def updateLastPoemId(stateId: Long, lastPoemId: Long) = inTransaction {
    update(serverstate)(p =>
      where(p.id === stateId)
      set(p.lastPoem := lastPoemId)
    )
  }

  def delete(id: Long): Int = inTransaction {
    serverstate.deleteWhere(p => p.id === id)
  }

  implicit val serverstateWrites: Writes[ServerState] = Json.writes[ServerState]

}


case class ServerState(id: Long = 1, lastPoem: Long = 1)


package models

import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Schema
import org.squeryl.annotations.Column
import org.squeryl.dsl._

object DBSchema extends Schema {

  val serverstate = table[ServerState]("serverstate")

}


package models

import play.api.libs.json._
import play.api.libs.functional.syntax._

object MetaPoem {

  implicit val metapoemReads: Reads[MetaPoem] = Json.reads[MetaPoem]

}

case class MetaPoem(id: Long, text: String)



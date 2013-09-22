package com.rumblesan.metapiano

import play.api.libs.concurrent._
import akka.actor._

import play.api.Play.current

import scala.concurrent.duration._
import scala.concurrent._
import ExecutionContext.Implicits.global

import play.api.Logger

import play.api.libs.ws.WS

import models.{ MetaPoem, ServerState }


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


class PoemRetriever extends Actor {

  var scheduledOpt: Option[Cancellable] = None

  val startDuration = 1 minutes
  val fetchDuration = 30 minutes

  val poemUrl = current.configuration.getString("metapiano.site.poemendpoint").get
  val poemIdParam = current.configuration.getString("metapiano.site.poemidparam").get
  val poemDownload = current.configuration.getBoolean("metapiano.site.poemdownload").get

  val poemFolder = current.configuration.getString("metapiano.poemfolder").get

  def setup = {

    Logger.info("Retrieving poems")

    scheduledOpt =  Some(
      Akka.system.scheduler.schedule(
        startDuration,
        fetchDuration,
        self,
        RetrievePoems
      )
    )

  }

  def savePoemToFolder(poem: MetaPoem) {

    try {
      val pFile = new File(poemFolder + "/" + poem.id.toString)

      if (pFile.exists()) {
        Logger.error("File already exists for id %d".format(poem.id))
      } else {
        pFile.createNewFile()

        val fw: FileWriter = new FileWriter(pFile.getAbsoluteFile())
        val bw: BufferedWriter = new BufferedWriter(fw)
        bw.write(poem.text)
        bw.close()

        Logger.info("Saved poem to disk: " + poem.id.toString)

      }
    } catch {
      case ioe: IOException => ioe.printStackTrace()
    }

  }

  def getPoems = {

    if (poemDownload) {
      val state = ServerState.retrieveOne(
        1
      ).getOrElse(ServerState.create(ServerState(1, 0)))

      val requestUrl = poemUrl + "?%s=%s".format(poemIdParam, state.lastPoem)

      Logger.info("Downloading poems from %s".format(requestUrl))

      WS.url(requestUrl).get().map { response =>
        Logger.info("Got response from poem site")
        response.json.validate[List[MetaPoem]].fold(
          invalid = (
            error => Logger.error(error.toString)
          ),
          valid = (
            poems => {
              Logger.info("Downloaded %s poems".format(poems.length.toString))
              for (p <- poems) {
                savePoemToFolder(
                  p.copy(text =
                    PoemTranslator.translatePoem(p.text)
                  )
                )
                Logger.info(p.toString)
              }
              if (poems.nonEmpty) {
                val maxId = poems.map(_.id).max
                Logger.info("Max poem id is %s".format(maxId.toString))
                ServerState.updateLastPoemId(1, poems.map(_.id).max)
              } else {
                Logger.info("No new poems to download")
              }
            }
          )
        )
      }

    }

  }


  def receive = {

    case StartRetrieving => setup

    case RetrievePoems => getPoems


  }

}


case object StartRetrieving
case object RetrievePoems
case object StopRetrieving


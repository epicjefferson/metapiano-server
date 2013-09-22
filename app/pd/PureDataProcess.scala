package com.rumblesan.scalapd

import akka.actor._

import scala.sys.process._

import util._


object PureData {

  def createPdArgs(exe:String,
                   port:Int,
                   patch:String,
                   paths:List[String],
                   extraArgs:List[String]): List[String] = {

      val basicArgs = List(exe,
                           "-open",
                           patch,
                           "-stderr",
                           "-nogui",
                           "-send",
                           "startup port %d".format(port))

      val fullPaths = paths.foldLeft(List.empty[String])(
        (total, current) => {
          "-path" :: current :: total
        }
      )

      basicArgs ::: fullPaths ::: extraArgs
  }

}

class PureDataProcess(loggingActor: Props) extends Actor {

  val listener = context.actorOf(loggingActor)

  val subprocess = context.actorOf(Props[SubProcess])

  var process: Option[Process] = None

  def receive = {

    case StartPD(exe, port, patch, paths, extraArgs, _) => {

      val args = PureData.createPdArgs(exe, port, patch, paths, extraArgs)

      val l = ProcessLogger(
        line => listener ! LogMessage(line),
        line => listener ! LogMessage(line)
      )

      val pb = Process(args)
      val p = pb.run(l)

      process = Some(p)

      subprocess ! SubProcessRun(p)

    }

    case KillPd() => {
      process.map(_.destroy())
      process = None
    }

  }

}

case class KillPd()
case class StartPD(exe:String, port:Int, patch:String, paths:List[String], extraArgs:List[String], responder: Option[ActorRef])



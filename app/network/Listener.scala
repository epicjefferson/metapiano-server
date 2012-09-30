package com.rumblesan.network


import akka.actor._

import org.jboss.netty.bootstrap.ServerBootstrap
import java.net.InetSocketAddress

class NetworkServer(port: Int, master: ActorRef) extends Actor with NettyServer {

  val bootstrap = getBootstrap(port, master)

  def receive = {

    case Start() => {
      println("Starting Network Coms")
    }

  }

}

case class Start()


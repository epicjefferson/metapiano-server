package com.rumblesan.network

import org.jboss.netty.channel._

import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder
import org.jboss.netty.handler.codec.string.{ StringDecoder, StringEncoder }

import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory
import org.jboss.netty.buffer.ChannelBuffers
import org.jboss.netty.bootstrap.ServerBootstrap

import java.net.InetSocketAddress
import java.nio.charset.Charset
import java.util.concurrent.Executors

import akka.actor._

object NettyServer {

  def getBootstrap(port: Int, master: ActorRef): ServerBootstrap = {

    val factory: ChannelFactory = new NioServerSocketChannelFactory(
      Executors.newCachedThreadPool(),
      Executors.newCachedThreadPool()
    )

    val bootstrap = new ServerBootstrap(factory)

    bootstrap.setPipelineFactory(new PDPipelineFactory(master))

    bootstrap.setOption("child.tcpNoDelay", true)
    bootstrap.setOption("child.keepAlive", true)

    bootstrap.bind(new InetSocketAddress(port))

    bootstrap
  }

}

class PDPipelineFactory(actor: ActorRef) extends ChannelPipelineFactory {

  val master = actor

  val messageDelimeter = ChannelBuffers.copiedBuffer(";\n", Charset.forName("UTF-8"))

  def getPipeline(): ChannelPipeline = {

    val pipeline = Channels.pipeline()

    pipeline.addLast("framer",  new DelimiterBasedFrameDecoder(1024, true, messageDelimeter))
    pipeline.addLast("decoder", new StringDecoder())
    pipeline.addLast("encoder", new StringEncoder())
    pipeline.addLast("handler", new PDMessageHandler(master))

    return pipeline
  }

}



class PDMessageHandler(actor: ActorRef) extends SimpleChannelHandler {

  val master = actor

  override def channelConnected(ctx: ChannelHandlerContext, e: ChannelStateEvent) = {
    val ch: Channel = e.getChannel()

    val pdChannel = new PDChannel(ch)

    master ! PDConnection(pdChannel)
  }

  override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent) = {
    val ch: Channel = e.getChannel()

    val message = e.getMessage().asInstanceOf[String].split(" ").toList

    master ! PDMessage(message)
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, e: ExceptionEvent) {
    e.getCause().printStackTrace()

    val ch: Channel = e.getChannel()
    ch.close()
  }

}

class PDChannel(ch: Channel) {
  val channel = ch

  def write(message: List[String]) = {
    val text = message.reduceLeft(_ + " " + _) + ";\n"
    val data = ChannelBuffers.copiedBuffer(text, Charset.forName("UTF-8"))
    ch.write(data)
  }

}

case class PDConnection(channel: PDChannel)
case class PDMessage(data: List[String])


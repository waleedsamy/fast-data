package de.traffics.fastdata

import java.util.concurrent.TimeUnit
import java.util.logging.{ Level, Logger }

import de.traffics.fastdata.proto.helloservice.{ HelloRequest, HelloServiceGrpc }
import de.traffics.fastdata.proto.helloservice.HelloServiceGrpc.HelloServiceBlockingStub
import io.grpc.{ ManagedChannel, ManagedChannelBuilder, StatusRuntimeException }

object HelloServiceClient {
  def apply(host: String, port: Int): HelloServiceClient = {
    val channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext(true).build
    val blockingStub = HelloServiceGrpc.blockingStub(channel)
    new HelloServiceClient(channel, blockingStub)
  }

  def main(args: Array[String]): Unit = {
    val client = HelloServiceClient("localhost", 50051)
    try {
      val user = args.headOption.getOrElse("world!")
      client.greet(user)
    } finally {
      client.shutdown()
    }
  }
}

class HelloServiceClient private (
  private val channel: ManagedChannel,
  private val blockingStub: HelloServiceBlockingStub) {
  private[this] val logger = Logger.getLogger(classOf[HelloServiceClient].getName)

  def shutdown(): Unit = {
    channel.shutdown.awaitTermination(5, TimeUnit.SECONDS)
  }

  /** Say hello to server. */
  def greet(name: String): Unit = {
    logger.info("Will try to greet " + name + " ...")
    val request = HelloRequest(greeting = name)
    try {
      val response = blockingStub.sayHello(request)
      logger.info("Greeting: " + response.reply)
    } catch {
      case e: StatusRuntimeException =>
        logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus)
    }
  }
}
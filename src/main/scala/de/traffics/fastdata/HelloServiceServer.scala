package de.traffics.fastdata

import java.util.logging.Logger

import de.traffics.fastdata.proto.helloservice.{ HelloRequest, HelloResponse, HelloServiceGrpc }
import io.grpc.{ Server, ServerBuilder }

import scala.concurrent.{ ExecutionContext, Future }

object HelloServiceServer {
  private val logger = Logger.getLogger(classOf[HelloServiceServer].getName)

  def main(args: Array[String]): Unit = {
    val server = new HelloServiceServer(ExecutionContext.global)
    server.start()
    server.blockUntilShutdown()
  }

  private val port = 50051
}

class HelloServiceServer(executionContext: ExecutionContext) { self =>
  private[this] var server: Server = null

  private def start(): Unit = {
    server = ServerBuilder.forPort(HelloServiceServer.port).addService(HelloServiceGrpc.bindService(new HelloServiceImpl, executionContext)).build.start
    HelloServiceServer.logger.info("Server started, listening on " + HelloServiceServer.port)
    sys.addShutdownHook {
      System.err.println("*** shutting down gRPC server since JVM is shutting down")
      self.stop()
      System.err.println("*** server shut down")
    }
  }

  private def stop(): Unit = {
    if (server != null) {
      server.shutdown()
    }
  }

  private def blockUntilShutdown(): Unit = {
    if (server != null) {
      server.awaitTermination()
    }
  }

  private class HelloServiceImpl extends HelloServiceGrpc.HelloService {
    override def sayHello(req: HelloRequest) = {
      val reply = HelloResponse(reply = "Hello " + req.greeting)
      Future.successful(reply)
    }
  }
}

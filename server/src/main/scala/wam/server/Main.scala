package wam.server

import java.util.concurrent.CountDownLatch

import org.http4s.StaticFile
import org.http4s.dsl._
import org.http4s.server.HttpService
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.server.syntax.ServiceOps
import wam.server.cache.InMemoryResponseCache
import wam.shared.WAMEvent

import scalaz.concurrent.Task
import scalaz.stream.async._
import scalaz.stream.async.mutable.Topic


object Main extends App with InMemoryResponseCache with Driver with CoDriver {
  override val wamEvents: Topic[WAMEvent] = topic[WAMEvent]()

  val commonService = HttpService {
    case req@GET -> Root / "client-fastopt.js" =>
      StaticFile.fromResource(s"/client-fastopt.js", Some(req)).fold(NotFound(s"script not found"))(Task.now)
  }

  Config(args) match {
    case Some(cfg) =>
      val latch = new CountDownLatch(2)

      // startup the driver
      BlazeBuilder.bindHttp(8000, "0.0.0.0")
        .withWebSockets(true)
        .mountService(driverService(cfg.host, cfg.port) orElse commonService, "/")
        .run
        .onShutdown(latch.countDown())

      // startup the co-driver
      BlazeBuilder.bindHttp(8001, "0.0.0.0")
        .withWebSockets(true)
        .mountService(coDriverService orElse commonService, "/")
        .run
        .onShutdown(latch.countDown())

      // wait for shutdown
      latch.await()

    case _ =>
  }
}

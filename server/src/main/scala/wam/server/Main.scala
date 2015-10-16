package wam.server

import java.util.concurrent.CountDownLatch

import org.http4s.dsl._
import org.http4s.server.HttpService
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.server.syntax.ServiceOps
import org.http4s.{Response, StaticFile}
import org.slf4j.LoggerFactory
import wam.server.cache.InMemoryResponseCache
import wam.shared.WAMEvent

import scalaz.concurrent.Task
import scalaz.stream.async._
import scalaz.stream.async.mutable.Topic


object Main extends App with InMemoryResponseCache with Driver with CoDriver with CoDriverLock {
  override val wamEvents: Topic[WAMEvent] = topic[WAMEvent]()

  private val logger = LoggerFactory.getLogger(getClass)

  val commonService = HttpService {
    case req@GET -> Root / "wam-app.js" =>
      def fromFile(path: String): Option[Response] = StaticFile.fromResource(path, Some(req))
      fromFile("/client-fastopt.js").orElse(fromFile("/client-opt.js")).fold(NotFound(s"wam-app.js not found"))(Task.now)
  }


  Config(args) match {
    case Some(cfg) =>
      val latch = new CountDownLatch(2)

      // startup the driver
      BlazeBuilder.bindHttp(8000, "0.0.0.0")
        .withWebSockets(true)
        .mountService(lockCoDriver(driverService(cfg)) orElse commonService, "/")
        .run
        .onShutdown(latch.countDown())

      // startup the co-driver
      BlazeBuilder.bindHttp(8001, "0.0.0.0")
        .withWebSockets(true)
        .mountService(coDriverLock(coDriverService(cfg)) orElse commonService, "/")
        .run
        .onShutdown(latch.countDown())

      // wait for shutdown
      latch.await()

    case _ =>
  }
}

package wam.server

import org.http4s.Uri
import org.http4s.server._
import org.slf4j.LoggerFactory
import scalaz.concurrent.Task

/**
 * co-driver's requests should be processed after the driver has
 * received his response, to ensure that the actual / newest response
 * for a given url is cached.
 *
 * = Usage:
 * {{{
 *   // set a lock for the actual request uri.
 *   ... lockCoDriver(driversService) ...
 *
 *
 *   // coDriversService execution are locked till drivers request
 *   // for a given url are processed.
 *   ... coDriverLock(coDriversService) ...
 * }}}
 */
trait CoDriverLock {

  private val logger = LoggerFactory.getLogger(getClass)

  private val inProcess = new scala.collection.mutable.ListBuffer[Uri]()

  /**
   * set a lock for the actual request uri. the lock is removed,
   * when the request are processed
   */
  def lockCoDriver(service: HttpService) = HttpService.lift { req =>
    inProcess.append(req.uri)
    val response = service(req)
    inProcess.synchronized(inProcess.remove(inProcess.indexOf(req.uri)))
    response
  }

  /**
   * blocks a request for the actual request uri if drivers request for this
   * url are in process.
   */
  def coDriverLock(service: HttpService): HttpService = HttpService.lift { req =>
    import scala.concurrent.duration._
    if (inProcess.contains(req.uri)) Task.schedule({
      logger.info(s"driver request (${req.uri}) in process - waiting")
      coDriverLock(service)(req)
    }, 500.millis).flatMap(identity)
    else service(req)
  }
}

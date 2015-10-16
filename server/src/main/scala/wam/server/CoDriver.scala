package wam.server

import org.http4s.dsl._
import org.http4s.server.HttpService
import org.http4s.server.websocket.WS
import org.http4s.websocket.WebsocketBits.Text
import org.http4s.{Response, Uri}
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import scodec.Codec
import scodec.codecs.implicits._
import wam.server.cache.{NotCachedException, ResponseCache}
import wam.server.ops._
import wam.shared._

import scala.concurrent.duration._
import scalaz.\/
import scalaz.concurrent.Task
import scalaz.stream._
import scalaz.stream.async.mutable.Topic

trait CoDriver {
  self: ResponseCache =>

  val wamEvents: Topic[WAMEvent]

  private val logger = LoggerFactory.getLogger(getClass)

  val coDriverService: Configured[HttpService] = { cfg =>
    HttpService {
      case req@GET -> Root / "wam-events" =>
        val src: Process[Task, Text] = wamEvents.subscribe.map(e => Text(Codec[WAMEvent].encode(e).require.toHex))
        WS(Exchange(src, Process.halt))

      case req if req.isAppEntryPoint(cfg.root) =>

        val fragment =
          """
            |<script src="/wam-app.js"></script>
            |<span id="face-cursor">&#8598;</span>
            |<span id="driver-window-border"></span>
            |<script type="text/javascript">
            |  wam.client.CoDriver().main()
            |</script>
          """.
            stripMargin

        val content: Throwable \/ Response = for {
          res <- responseFromCache(req.uri)
          txt <- res.as[String].attemptRun
          doc <- Task(Jsoup.parse(txt)).attemptRun
          _ = doc.body.append(fragment)
        } yield res.withBody(doc).run

        content.fold(e => InternalServerError(e.getMessage), Task.now)

      case req =>
        // FIXME: remove this hack
        def tryServeFromCache(uri: Uri, attempt: Int): Task[Response] = responseFromCache(uri).fold(_ match {
          case _: NotCachedException if (attempt > 0) =>
            logger.info(s"content for uri: ${uri} not avaliable... recheck ${attempt} times")
            // FIXME: delay configurable
            Task.schedule(tryServeFromCache(uri, attempt - 1), 1.second).flatMap(identity)
          case e => InternalServerError(e.getMessage)
        }
          , Task.now)

        tryServeFromCache(req.uri, 10)
    }
  }
}

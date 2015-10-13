package wam.server

import org.http4s.{StaticFile, Response}
import org.http4s.dsl._
import org.http4s.server.HttpService
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.server.websocket.WS
import org.http4s.websocket.WebsocketBits.Text

import org.jsoup.Jsoup
import scodec.Codec
import scodec.codecs.implicits._

import scalaz.\/
import scalaz.concurrent.Task
import scalaz.stream._
import scalaz.stream.async.mutable.Topic

import wam.server.ops._
import wam.server.cache.ResponseCache
import wam.shared._

trait CoDriver {
  self: ResponseCache =>

  val wamEvents: Topic[WAMEvent]

  val coDriverService = HttpService {
    case req@GET -> Root / "wam-events" =>
      val src: Process[Task, Text] = wamEvents.subscribe.map(e => Text(Codec[WAMEvent].encode(e).require.toHex))
      WS(Exchange(src, Process.halt))

    case req if req.isAppEntryPoint =>

      val fragment =
        """
          |<script src="/client-fastopt.js"></script>
          |<span id="face-cursor">&#8598;</span>
          |<script type="text/javascript">
          |  wam.client.CoDriver().main()
          |</script>
        """.stripMargin

      val content: Throwable \/ Response = for {
        res <- responseFromCache(req.uri)
        txt <- res.as[String].attemptRun
        doc <- Task(Jsoup.parse(txt)).attemptRun
        _ = doc.body.append(fragment)
      } yield res.withBody(doc).run

      content.fold(e => InternalServerError(e.getMessage), Task.now)

    case req =>
      responseFromCache(req.uri).fold(e => InternalServerError(e.getMessage), Task.now)
  }
}

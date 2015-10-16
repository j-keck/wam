package wam.server

import org.http4s.{StaticFile, Response}
import org.http4s.Uri.{RegName, Authority}
import org.http4s.client.blaze._
import org.http4s.dsl._
import org.http4s.server.HttpService
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.server.websocket.WS
import org.http4s.websocket.WebsocketBits.{Text, WebSocketFrame}

import org.jsoup.Jsoup
import scodec.Codec
import scodec.bits.BitVector
import scodec.codecs.implicits._
import scodec.interop.scalaz._

import scalaz.\/
import scalaz.syntax.either._
import scalaz.concurrent.Task
import scalaz.stream._
import scalaz.stream.async.mutable.Topic

import wam.server.ops._
import wam.server.cache.ResponseCache
import wam.shared._

trait Driver {
  self: ResponseCache =>

  val wamEvents: Topic[WAMEvent]

  def driverService: Configured[HttpService] = { cfg =>
    HttpService {
      case req@GET -> Root / "wam-events" =>

        val snk: Channel[Task, WebSocketFrame, Unit] = wamEvents.publish.contramap(_ match {
          case Text(txt, _) => (for {
            b <- BitVector.fromHex(txt).fold[String \/ BitVector]("invalid hex string received".left)(_.right)
            c <- Codec[WAMEvent].decodeValue(b).toDisjunction.leftMap(_.message)
          } yield c).fold(msg => throw new Exception(msg), identity)
        })

        WS(Exchange(Process.halt, snk))


      case req if req.isAppEntryPoint(cfg.root) =>

        val fragment =
          """
            |<script src="/wam-app.js"></script>
            |<script type="text/javascript">
            |  wam.client.Driver().main()
            |</script>
          """.
            stripMargin


      val content: Throwable \/ Response = for {
        res <- defaultClient(req.uri.withHost(cfg.host, cfg.port)).map(cacheResponse(req.uri, _)).attemptRun
        txt <- res.as[String].attemptRun
        doc <- Task(Jsoup.parse(txt)).attemptRun
        _ = doc.body.append(fragment)
      } yield res.withBody(doc).run

        content.fold(e => InternalServerError(e.getMessage), Task.now)

      case req =>

      // proxy
      val content: Throwable \/ Response = for {
        res <- defaultClient(req.uri.withHost(cfg.host, cfg.port)).attemptRun
      } yield cacheResponse(req.uri, res)

        content.fold(e => InternalServerError(e.getMessage), Task.now)
    }
  }
}

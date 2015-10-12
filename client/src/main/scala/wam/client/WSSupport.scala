package wam.client

import org.scalajs.dom
import org.scalajs.dom._
import org.scalajs.dom.raw.WebSocket
import scodec.Codec
import scodec.bits.BitVector
import scodec.codecs.implicits._

import wam.shared._

trait WSSupport {
  self: log.LogSupport =>

  lazy val ws = new {
    private val socket = webSocket("wam-events")
    socket.onopen = (e: Event) => debug(s"ws connected")
    socket.onerror = (e: ErrorEvent) => error(s"ws error: ${e.message}")
    socket.onclose = (e: Event) => debug(s"ws closed")

    def send(e: WAMEvent): Unit = {
      debug(s"send msg: $e")
      socket.send(Codec[WAMEvent].encode(e).require.toHex)
    }

    def on(pf: PartialFunction[WAMEvent, Unit]): Unit = {
      socket.onmessage = (e: MessageEvent) => {
        pf(Codec[WAMEvent].decodeValue(BitVector.fromHex(e.data.toString).get).require)
      }
    }

    private def webSocket(endpoint: String): WebSocket = {
      val proto = if (dom.document.location.protocol == "https:") "wss" else "ws"
      val url = s"${proto}://${dom.document.location.host}/${endpoint}"
      new WebSocket(url)
    }
  }
}


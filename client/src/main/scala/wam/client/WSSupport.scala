package wam.client

import java.util.concurrent.atomic.AtomicInteger

import org.scalajs.dom
import org.scalajs.dom._
import org.scalajs.dom.raw.WebSocket
import scodec.Codec
import scodec.bits.BitVector
import scodec.codecs.implicits._


import wam.shared._

trait WSSupport {
  self: log.LogSupport =>

  private val mutex = new AtomicInteger(1)
  private val socket = webSocket("wam-events")
  socket.onopen = (e: Event) => {
    info(s"ws connected")
    mutex.decrementAndGet()
  }
  socket.onerror = (e: ErrorEvent) => error(s"ws error: ${e.message}")
  socket.onclose = (e: Event) => info(s"ws closed")

  val ws = new {
    def send(e: WAMEvent): Unit = {
      if(mutex.intValue() == 0){
        socket.send(Codec[WAMEvent].encode(e).require.toHex)
      }else{
        dom.setTimeout(() => send(e), 100)
      }
    }

    def on(pf: PartialFunction[WAMEvent, Unit]): Unit = {
      socket.onmessage = (e: MessageEvent) => {
        pf(Codec[WAMEvent].decodeValue(BitVector.fromHex(e.data.toString).get).require)
      }
    }

  }


  private def webSocket(endpoint: String): WebSocket = {
    val proto = if (dom.document.location.protocol == "https:") "wss" else "ws"
    val url = s"${proto}://${dom.document.location.host}/${endpoint}"
    new WebSocket(url)
  }
}


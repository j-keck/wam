package wam.client

import sodium.{Stream, CellSink}
import wam.shared

import scala.scalajs.js.JSApp
import org.scalajs.dom
import org.scalajs.dom._

import wam.client.log._
import wam.client.ops._
import wam.shared._
import scala.concurrent.duration._

object Driver extends JSApp with Log2Console with WSSupport {


  private val doc = dom.document

  def main(): Unit = {
    type MousePos = (Double, Double)
    type ScrollPos = (Int, Int)

    val mousePos = new CellSink[MousePos](0, 0)
    doc.onmousemove = (e: MouseEvent) => {
      mousePos.send(e.clientX -> e.clientY)
    }

    val mouseClick = new CellSink[Boolean](false)
    doc.onmousedown = (e: MouseEvent) => {
      mouseClick.send(e.fromLeftBtn)
      mouseClick.send(false)
    }


    val scroll = new CellSink[ScrollPos](0, 0)
    doc.onscroll = (e: UIEvent) => {
      scroll.send(doc.body.scrollTop.toInt -> doc.body.scrollLeft.toInt)
    }


    // FIXME: throttle time configurable
    mousePos.throttle(100.millis).map{case (x, y) => MouseMoveEvent(x, y)}.updates.listen(ws.send)

    mouseClick.zip(mousePos).filterMap{ case (true, (x, y)) => MouseClickEvent(x, y)}.listen(ws.send)

    scroll.updates.map{case (x, y) => ScrollEvent(x, y)}.listen(ws.send)
  }

}

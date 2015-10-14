package wam.client


import org.scalajs.dom.raw.HTMLInputElement
import sodium.{Stream, CellSink}
import wam.shared

import scala.scalajs.js.JSApp
import org.scalajs.dom
import org.scalajs.dom._

import wam.client.log._
import wam.client.ops._
import wam.shared._
import scala.concurrent.duration._
import scala.util.Try

object Driver extends JSApp with Log2Console with WSSupport {


  private val doc = dom.document

  def main(): Unit = {
    type MousePos = (Double, Double)
    val mousePos = new CellSink[MousePos](0, 0)
    doc.onmousemove = (e: MouseEvent) => {
      mousePos.send(e.clientX -> e.clientY)
    }

    val mouseClick = new CellSink[Boolean](false)
    doc.onmousedown = (e: MouseEvent) => {
      mouseClick.send(e.fromLeftBtn)
      mouseClick.send(false)
    }


    val scroll = new CellSink[ScrollEvent](ScrollEvent(0, 0))
    doc.onscroll = (e: UIEvent) => {
      scroll.send(ScrollEvent(doc.body.scrollTop.toInt, doc.body.scrollLeft.toInt))
    }


    val input = new CellSink[TextInput](TextInput(""))
    doc.oninput = (e: Event) => {
      if(e.target.isInstanceOf[HTMLInputElement]){
        val value = e.target.asInstanceOf[HTMLInputElement].value
        input.send(TextInput(value))
      }else{
        error(s"unexpected target element for input: ${e.target}")
      }
    }

    val windowSize = new CellSink[WindowSize](WindowSize(dom.innerWidth, dom.innerHeight))
    dom.onresize = (e: UIEvent) => {
      windowSize.send(WindowSize(dom.innerWidth, dom.innerHeight))
    }


    // FIXME: throttle time configurable
    mousePos.throttle(100.millis).map{case (x, y) => MouseMoveEvent(x, y)}.updates.listen(ws.send)

    mouseClick.zip(mousePos).filterMap{ case (true, (x, y)) => MouseClickEvent(x, y)}.listen(ws.send)

    scroll.updates.listen(ws.send)

    input.updates.listen(ws.send)

    // send window resize updates and every 5sec. also, so later connected co-driver
    // receives the window size also.
    windowSize.repeat(5.second).value.listen(ws.send)
  }

}

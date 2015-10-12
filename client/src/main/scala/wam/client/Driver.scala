package wam.client

import scala.scalajs.js.JSApp
import org.scalajs.dom
import org.scalajs.dom._

import wam.client.log._
import wam.shared._

object Driver extends JSApp with Log2Console with WSSupport {

  private val doc = dom.document
  def main(): Unit = {
    doc.onmousemove = (e: MouseEvent) => {
      ws.send(MouseMoveEvent(e.clientX, e.clientY))
    }

    doc.onmouseup = (e: MouseEvent) => {
      ws.send(MouseRelease(e.button))
    }
    doc.onmousedown = (e: MouseEvent) => {
      ws.send(MouseDown(e.button))
    }

    doc.onscroll = (e: UIEvent) => {
      ws.send(ScrollEvent(doc.body.scrollTop.toInt, doc.body.scrollLeft.toInt))
    }
  }
}

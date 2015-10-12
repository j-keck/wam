package wam.client

import org.scalajs.dom
import org.scalajs.dom._

import wam.client.log.Log2Console
import wam.shared._

import scala.scalajs.js
import scala.scalajs.js.JSApp



object CoDriver extends JSApp with Log2Console with WSSupport {

  val doc = dom.document
  var x, y = 0.0

  def main(): Unit = {
    ws.on {
      case MouseMoveEvent(x, y) =>
        this.x = x
        this.y = y
        val style = s"position: absolute; left: ${x}px; top: ${y}px;"
        doc.getElementById("face-cursor").setAttribute("style", style)
      case MouseDown(0) =>
        val evt = document.createEvent("MouseEvents")
        evt.initEvent("click", true, true)

        val elem = doc.elementFromPoint(x - 1, y - 1)
        elem.dispatchEvent(evt)
      case ScrollEvent(top, left) =>
        dom.window.scrollTo(left, top)
      case _ => // ignore
    }
  }

}

package wam.client

import org.scalajs.dom
import org.scalajs.dom._

import wam.client.log.Log2Console
import wam.shared._
import wam.client.ops._

import scala.scalajs.js
import scala.scalajs.js.JSApp


object CoDriver extends JSApp with Log2Console with WSSupport {

  val doc = dom.document


  def main(): Unit = {
    ws.on {
      case MouseMoveEvent(x, y) =>
        val style = s"position: absolute; left: ${x}px; top: ${y}px;"
        faceCursor.setAttribute("style", style)

      case MouseClickEvent(x, y) =>
        val evt = document.createEvent("MouseEvents")
        evt.initEvent("click", true, true)

        // hide the face-cursor, because it's the top element at point (x, y)
        faceCursor.hide

        val elem = doc.elementFromPoint(x, y)
        elem.dispatchEvent(evt)

        // show it again
        faceCursor.show

      case ScrollEvent(top, left) =>
        dom.window.scrollTo(left, top)
      case msg => error(s"unexpected WAMEvent: '${msg}")
    }
  }

  private def faceCursor: Element = doc.getElementById("face-cursor")

}

package wam.client

import org.scalajs.dom
import org.scalajs.dom._
import org.scalajs.dom.raw.HTMLInputElement

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
        val elem = elementFromPoint(x, y)
        if(elem.isInstanceOf[HTMLInputElement]){
          // it's a input element - trigger focus
          elem.asInstanceOf[HTMLInputElement].focus()
        }else {
          // trigger a click event
          val evt = document.createEvent("MouseEvents")
          evt.initEvent("click", true, true)
          elem.dispatchEvent(evt)
        }

      case ScrollEvent(top, left) =>
        dom.window.scrollTo(left, top)

      case TextInput(text) =>
        if(doc.activeElement.isInstanceOf[HTMLInputElement]){
          doc.activeElement.asInstanceOf[HTMLInputElement].value = text
        }else{
          error(s"input event but no input element active - active element: ${doc.activeElement}")
        }

      case msg => error(s"unexpected WAMEvent: '${msg}")
    }
  }

  private def faceCursor: Element = doc.getElementById("face-cursor")

  private def elementFromPoint(x: Double, y: Double): Element = {
    // hide the face-cursor, because it's the top element at point (x, y)
    faceCursor.hide

    val elem = doc.elementFromPoint(x, y)

    // show it again
    faceCursor.show

    elem
  }

}

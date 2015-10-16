package wam.client

import org.scalajs.dom
import org.scalajs.dom._
import org.scalajs.dom.raw.{HTMLButtonElement, HTMLFormElement, HTMLInputElement}

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
        elementFromPoint(x, y) match {
          case e: HTMLButtonElement => e.click()
          case e: HTMLInputElement if e.`type` == "submit" => e.form.submit()
          case e: HTMLInputElement => e.focus()
          case elem =>
            // trigger a click event
            val evt = document.createEvent("MouseEvents")
            evt.initEvent("click", true, true)
            elem.dispatchEvent(evt)
        }

      case ScrollEvent(top, left) =>
        dom.window.scrollTo(left, top)

      case TextInput(text) => doc.activeElement match {
        case e: HTMLInputElement => e.value = text
        case e => error(s"input event but no input element active - active element: ${e}")
      }

      case WindowSize(width, height) =>
        val style = s"position: absolute; left: 0px; top: 0px; width: ${width}px; height: ${height}px;border: 1px solid red; z-index: -1;"
        doc.getElementById("driver-window-border").setAttribute("style", style)

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

package wam.client

import org.scalajs.dom
import org.scalajs.dom._
import sodium.{CellSink, Stream, Cell}

import scala.concurrent.duration.Duration

object ops {


  implicit class CellOps[A](val ca: Cell[A]) extends AnyVal {
    def zip[B](cb: Cell[B]): Cell[(A, B)] =
      ca.lift((a, b: B) => (a, b), cb)

    /** scala collection's 'collect' */
    def filterMap[B](pf: PartialFunction[A, B]): Stream[B] = {
      ca.updates.filter(pf.isDefinedAt).map(pf)
    }

    def throttle(duration: Duration): Cell[A] = {
      var last = ca.sample
      every(duration).map(_ => ca.sample).value.filter { cur =>
        val res = cur != last
        last = cur
        res
      }.hold(last)
    }

    def repeat(duration: Duration): Cell[A] = {
      every(duration).map(_ => ca.sample)
    }

    private def every(duration: Duration): Cell[Long] = {
      val ticker = new CellSink[Long](System.currentTimeMillis())
      def timer(): Unit = {
        dom.setTimeout(() => {
          ticker.send(System.currentTimeMillis())
          timer
        }, duration.toMillis)
      }
      timer()

      ticker
    }
  }

  implicit class StreamOps[A](val sa: Stream[A]) extends AnyVal {
    def accum[S](s: S)(f: (A, S) => S): Cell[S] = sa.accum(s, f)
  }


  implicit class MouseEventOps(val e: MouseEvent) extends AnyVal {
    def fromLeftBtn: Boolean = e.button == 0
  }

  implicit class ElementOps(val e: Element) extends AnyVal {

    def hide: Unit = {
      val style = e.getAttribute("style")
      e.setAttribute("style", style + ";display: none;")
    }

    def show: Unit = {
      val style = e.getAttribute("style")
      e.setAttribute("style", style.replaceAll(";display: none;", ""))
    }
  }


}

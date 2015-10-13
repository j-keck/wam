package wam.client

import org.scalajs.dom
import org.scalajs.dom._

import scala.scalajs.js


object log {

  private val doc = dom.document

  trait LogSupport {

    def debug(msg: String) = log(now, "DEBUG", msg)

    def info(msg: String) = log(now, "INFO", msg)

    def error(msg: String) = log(now, "ERROR", msg)

    def exception(msg: String, e: Exception) = log(now, "EXCEPTION", s"${msg}: ${e.getMessage}")

    def log(ts: String, severity: String, msg: String): Unit

    private def now: String = new js.Date(js.Date.now()).toLocaleTimeString
  }


  trait Log2Console extends LogSupport {
    override def log(ts: String, severity: String, msg: String): Unit = {
      println(s"$ts | $severity | $msg")
    }
  }
}

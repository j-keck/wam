package wam.server

import scopt.OptionParser

case class Config(host: String = "", port: Int = 80)

object Config {
  private val parser = new OptionParser[Config]("wam") {
    head("wam", "0.0.1")
    opt[String]('h', "host") required() action{ (h, c) => c.copy(host = h)} text("host name from the wrapped app")
    opt[Int]('p', "port") action{ (p, c) => c.copy(port = p)} text("port from the wrapped app (default: 80)")
  }

  def apply(args: Array[String]): Option[Config] =
    parser.parse(args, Config())

}

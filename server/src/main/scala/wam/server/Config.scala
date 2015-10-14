package wam.server

import scopt.OptionParser

import java.util.Properties

case class Config(host: String = "", port: Int = 80)

object Config {
  private val wamVersion = {
    Option(ClassLoader.getSystemResourceAsStream("version.properties")).fold("<VERSION NOT FOUND>") { url =>
      val props = new Properties()
      props.load(url)
      props.getProperty("version")
    }
  }

  private val parser = new OptionParser[Config]("wam") {
    head("wam", wamVersion)
    opt[String]('h', "host") required() action{ (h, c) => c.copy(host = h)} text("host name from the wrapped app")
    opt[Int]('p', "port") action{ (p, c) => c.copy(port = p)} text("port from the wrapped app (default: 80)")
  }

  def apply(args: Array[String]): Option[Config] =
    parser.parse(args, Config())

}

package wam.server

import org.http4s.Uri.{RegName, Authority}
import org.http4s.headers.`Content-Type`
import org.http4s._
import org.http4s.EntityEncoder.Entity
import org.jsoup.nodes.Document



import scalaz.concurrent.Task

object ops {

  implicit val jsoupDocEncoder: EntityEncoder[org.jsoup.nodes.Document] =
    new EntityEncoder[org.jsoup.nodes.Document] {
      override def toEntity(a: Document): Task[Entity] = implicitly[EntityEncoder[String]].toEntity(a.outerHtml)

      override def headers: Headers = Headers(`Content-Type`(MediaType.`text/html`))
    }


  implicit class RequestOps(val req: Request) extends AnyVal {
    def isAppEntryPoint: Boolean =
      req.uri.path.equals("/") || Seq("index.html").exists(req.uri.path.endsWith)
  }

  implicit class UriOps(val uri: Uri) extends AnyVal {
    def withHost(host: String, port: Option[Int] = None) = {
      val userInfo = uri.authority.flatMap(_.userInfo)
      uri.copy(authority = Some(Authority(userInfo, RegName(host), port)))
    }
  }



}

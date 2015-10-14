package wam.server

import java.util.concurrent.ConcurrentHashMap

import org.http4s._
import scodec.bits.ByteVector
import scalaz.\/
import scalaz.syntax.either._
import scalaz.stream.Process

object cache {

  class CacheException(msg: String) extends Throwable(msg)
  class NotCachedException(msg: String) extends CacheException(msg)

  trait ResponseCache {
    def cacheResponse(uri: Uri, res: Response): Response

    def responseFromCache(uri: Uri): CacheException \/ Response
  }


  trait InMemoryResponseCache extends ResponseCache {

    private val cacheMap = new ConcurrentHashMap[Uri, Response]()

    def cacheResponse(uri: Uri, res: Response): Response = res.body.runLog.map { bytes =>
      // Collect the whole body to a primitive ByteVector view of a single Array[Byte]
      val bv = ByteVector.view(bytes.foldLeft(ByteVector.empty)(_ ++ _).toArray)
      val newResponse = res.copy(body = Process.emit(bv))
      cacheMap.put(uri, newResponse)
      newResponse
    }.run


    def responseFromCache(uri: Uri): CacheException \/ Response = Option (cacheMap.get(uri)).
      fold[CacheException \/ Response](new NotCachedException(s"not cached - uri: $uri").left)(_.right)
  }

}
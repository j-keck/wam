package wam.shared

import org.scalatest.{FunSuite, Matchers}
import scodec.Codec
import scodec.codecs.implicits._

class SerializationSuite extends FunSuite with Matchers {

  test("MouseMoveEvent"){
    val mme = MouseMoveEvent(1, 2)
    val mmeB = Codec.encode(mme).require
    Codec[MouseMoveEvent].decodeValue(mmeB).require should be(mme)
  }

  test("MouseClick"){
    val md = MouseClickEvent(1, 2)
    val mdB = Codec.encode(md).require
    Codec[MouseClickEvent].decodeValue(mdB).require should be(md)
  }

  test("WAMEvent"){
    def check(me: WAMEvent): Unit ={
      val b = Codec[WAMEvent].encode(me).require
      val c = Codec[WAMEvent].decodeValue(b).require
      c should be(me)
    }
    check(MouseMoveEvent(1.1, 2.2))
    check(MouseClickEvent(1, 2))
  }

}

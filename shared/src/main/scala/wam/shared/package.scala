package wam

import scodec.codecs._
import scodec.bits._

package object shared {

  /**
   * event which are propagated
   */
  sealed trait WAMEvent

  object WAMEvent {
    implicit val discriminated: Discriminated[WAMEvent, Int] = Discriminated(uint8)
  }


  /**
   * mouse move event
   */
  case class MouseMoveEvent(x: Double, y: Double) extends WAMEvent

  object MouseMoveEvent {
    implicit val discriminator: Discriminator[WAMEvent, MouseMoveEvent, Int] = Discriminator(1)
  }


  /**
   * mouse click event
   */
  case class MouseClickEvent(x: Double, y: Double) extends WAMEvent

  object MouseClickEvent {
    implicit val discriminator: Discriminator[WAMEvent, MouseClickEvent, Int] = Discriminator(2)
  }


  /**
   * page scroll event
   */
  case class ScrollEvent(top: Int, left: Int) extends WAMEvent

  object ScrollEvent {
    implicit val discriminator: Discriminator[WAMEvent, ScrollEvent, Int] = Discriminator(3)
  }

}

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
   * mouse click event: up / down
   */
  type MouseBtn = Int

  sealed trait MouseClickEvent extends WAMEvent {
    val btn: MouseBtn

    def isLeft = btn == 0

    def istMiddle = btn == 1

    def isRight = btn == 2
  }

  case class MouseDown(btn: MouseBtn) extends MouseClickEvent

  object MouseDown {
    implicit val discriminator: Discriminator[WAMEvent, MouseDown, Int] = Discriminator(2)
  }

  case class MouseRelease(btn: MouseBtn) extends MouseClickEvent

  object MouseRelease {
    implicit val discriminator: Discriminator[WAMEvent, MouseRelease, Int] = Discriminator(3)
  }


  /**
   * page scroll event
   */
  case class ScrollEvent(top: Int, left: Int) extends WAMEvent

  object ScrollEvent {
    implicit val discriminator: Discriminator[WAMEvent, ScrollEvent, Int] = Discriminator(4)
  }

}

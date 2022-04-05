package flames.concurrent.actor

import scala.util.control.NoStackTrace

case class Undelivered[T](message: T) extends Exception(
  s"Message $message was sent to stopped actor."
) with NoStackTrace
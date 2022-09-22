package flames.actors.message

enum Ack[+T] {
  case Delivered(response: T)
  case Undelivered(reason: DeliveryFailure) extends Ack[Nothing]
}
object Acks {
  import Ack.*

  val Ok: Delivered[Unit] = Ack.Delivered(())

  val Overflow: Undelivered = Ack.Undelivered(DeliveryFailure.Overflow)

  val DeadLetter: Undelivered = Ack.Undelivered(DeliveryFailure.DeadLetter)

  val TimedOut: Undelivered = Ack.Undelivered(DeliveryFailure.TimedOut)

  def connection(exc: Throwable): Undelivered = Ack.Undelivered(DeliveryFailure.Connection(exc))

}
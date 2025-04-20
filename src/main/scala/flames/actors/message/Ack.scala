package flames.actors.message

import flames.actors.pattern.Monad

sealed trait Ack[+T] {

  def map[R](f: T => R): Ack[R]

  def flatMap[R](f: T => Ack[R]): Ack[R]

}
object Ack {

  case class Delivered[T](response: T) extends Ack[T] {
    override def map[R](f: T => R): Ack[R] = Delivered(f(response))
    override def flatMap[R](f: T => Ack[R]): Ack[R] = f(response)
  }
  case class Undelivered(reason: DeliveryFailure) extends Ack[Nothing] {
    override def map[R](f: Nothing => R): Ack[R] = this
    override def flatMap[R](f: Nothing => Ack[R]): Ack[R] = this
  }

  given Monad[Ack] with
    override def pure[T](value: T): Ack[T] = Delivered(value)
    extension [T](self: Ack[T]) {
      override def map[R](f: T => R): Ack[R] = self.map(f)
      override def flatMap[R](f: T => Ack[R]): Ack[R] = self.flatMap(f)
    }
  end given

  val Ok: Delivered[Unit] = Ack.Delivered(())

  val Overflow: Undelivered = Ack.Undelivered(DeliveryFailure.Overflow)

  val DeadLetter: Undelivered = Ack.Undelivered(DeliveryFailure.DeadLetter)

  val TimedOut: Undelivered = Ack.Undelivered(DeliveryFailure.TimedOut)

  def connection(exc: Throwable): Undelivered = Ack.Undelivered(DeliveryFailure.Connection(exc))

}

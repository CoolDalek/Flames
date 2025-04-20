package flames.actors.pattern

import flames.actors.message.*
import flames.actors.message.Ack.*
import flames.actors.utils.SummonerK

// unlawful async monad 
trait Wait[F[_]] extends Monad[F] {

  def asyncAck[T](f: (DeliveryFailure | T => Unit) => Unit): F[Ack[T]]

  def async[T](f: (T => Unit) => Unit): F[T]

  def delivered[T](value: T): F[Delivered[T]]

  def undelivered[T](value: DeliveryFailure): F[Undelivered]

  def raise[T](exc: Throwable): F[T]

}

object Wait extends SummonerK[Wait]

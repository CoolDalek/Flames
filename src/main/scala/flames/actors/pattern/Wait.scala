package flames.actors.pattern

import flames.actors.message.*
import Ack.*

trait Wait[F[_]] {

  def sync[T](ack: Ack[T]): F[Ack[T]]

  def async[T](f: (DeliveryFailure | T => Unit) => Unit): F[Ack[T]]
  
  def delivered[T](value: T): F[Delivered[T]]

  def undelivered[T](value: DeliveryFailure): F[Undelivered]

  extension[T] (self: F[T]) {

    def map[R](f: T => R): F[R]

  }

}
object Wait {

  inline def apply[F[_]: Wait]: Wait[F] = summon[Wait[F]]

}

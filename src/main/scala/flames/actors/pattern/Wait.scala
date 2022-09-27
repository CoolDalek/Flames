package flames.actors.pattern

import flames.actors.message.*
import Ack.*
import flames.actors.utils.SummonerK

trait Wait[F[_]] {
  
  def asyncAck[T](f: (DeliveryFailure | T => Unit) => Unit): F[Ack[T]]
  
  def async[T](f: (T => Unit) => Unit): F[T]

  def delivered[T](value: T): F[Delivered[T]]

  def undelivered[T](value: DeliveryFailure): F[Undelivered]

  def lift[T](value: T): F[T]

  extension[T] (self: F[T]) {

    def map[R](f: T => R): F[R]

  }

}
object Wait extends SummonerK[Wait]

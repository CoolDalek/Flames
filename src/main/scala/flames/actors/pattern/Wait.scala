package flames.actors.pattern

import flames.actors.message.*
import flames.actors.message.Ack.*
import flames.actors.utils.SummonerK

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.control.NonFatal

// unlawful >:) async monad
trait Wait[F[_]] extends Monad[F]:

  def asyncAck[T](f: (DeliveryFailure | T => Unit) => Unit): F[Ack[T]]

  def async[T](f: (T => Unit) => Unit): F[T]

  def delivered[T](value: T): F[Delivered[T]]

  def undelivered[T](value: DeliveryFailure): F[Undelivered]

  def raise[T](exc: Throwable): F[T]

object Wait extends SummonerK[Wait]:

  given (using ExecutionContext): Wait[Future] with
    override def asyncAck[T](f: (DeliveryFailure | T => Unit) => Unit): Future[Ack[T]] =
      async[Ack[T]] { cb =>
        f {
          case failure: DeliveryFailure => cb(Undelivered(failure))
          case success: T => cb(Delivered(success))
        }
      }
    override def async[T](f: (T => Unit) => Unit): Future[T] =
      val promise = Promise[T]()
      try f(promise.success)
      catch case NonFatal(exc) => promise.failure(exc)
      promise.future
    end async
    override def delivered[T](value: T): Future[Delivered[T]] =
      Future.successful(Delivered(value))
    override def undelivered[T](value: DeliveryFailure): Future[Undelivered] =
      Future.successful(Undelivered(value))
    override def raise[T](exc: Throwable): Future[T] = Future.failed(exc)
    override def pure[T](value: T): Future[T] = Future.successful(value)
    extension [T](self: Future[T]) {
      override def map[R](f: T => R): Future[R] = self.map(f)
      override def flatMap[R](f: T => Future[R]): Future[R] = self.flatMap(f)
    }
  end given

end Wait

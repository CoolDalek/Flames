package flames.concurrent

import flames.util.SummonerK

import scala.concurrent.{ExecutionContext, Future}

trait Wrap[F[_]] {

  def wrap[T](value: => T): F[T]

}

object Wrap extends SummonerK[Wrap]

given (using ec: ExecutionContext): Wrap[Future] = new Wrap[Future] {
  inline override def wrap[T](value: => T): Future[T] = Future(value)
}
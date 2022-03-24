package flames.concurrent

import scala.concurrent.duration.FiniteDuration

trait WorkerF[T, F[_]: AskWorker: Wrap] extends Worker[T] {

  def ask[Res](timeout: FiniteDuration)(request: Worker[Res] => T): F[Res] =
    AskWorker[F].ask(this, timeout)(request)

  def tellF(message: T): F[Unit] =
    Wrap[F].wrap(tell(message))

}

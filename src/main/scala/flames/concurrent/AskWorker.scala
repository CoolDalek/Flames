package flames.concurrent

import flames.util.SummonerK

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Future, Promise, TimeoutException}
import scala.util.Success

trait AskWorker[F[_]] {

  def ask[Req, Res](worker: Worker[Req], timeout: FiniteDuration)
                   (request: Worker[Res] => Req)
                   (using WorkersRuntime): F[Res]

}

object AskWorker extends SummonerK[AskWorker] {

  private[concurrent] class PromiseWorker[T](promise: Promise[T],
                                             timeout: FiniteDuration)
                                            (using WorkersRuntime) extends Worker[T] {

    private val deadline =
      runtime.schedule(timeout) {
        promise.failure(new TimeoutException)
      }

    override def act(): Behavior[T] =
      receive { result =>
        deadline.cancel()
        promise.success(result)
        stop
      }

  }

}

extension [Req](worker: Worker[Req]) {

  inline def ask[F[_]: AskWorker, Res](timeout: FiniteDuration)
                                      (request: Worker[Res] => Req)
                                      (using WorkersRuntime): F[Res] =
    AskWorker[F].ask(worker, timeout)(request)

}

given AskWorker[Future] = new AskWorker[Future] {

  override def ask[Req, Res](worker: Worker[Req], timeout: FiniteDuration)
                            (request: Worker[Res] => Req)
                            (using WorkersRuntime): Future[Res] = {
    val promise = Promise[Res]()
    val waiter = AskWorker.PromiseWorker(promise, timeout)
    val message = request(waiter)
    worker.tell(message)
    promise.future
  }

}
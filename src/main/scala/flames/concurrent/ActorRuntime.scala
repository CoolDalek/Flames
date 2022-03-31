package flames.concurrent

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.ExecutionContext
import flames.util.Logger

import scala.collection.concurrent.TrieMap
import scala.collection.mutable

final class ActorRuntime private(
                                  makeLogger: ActorRuntime ?=> Logger,
                                  scheduler: Scheduler,
                                  val autoYieldCount: Int = 8,
                                  val autoYieldTime: Option[FiniteDuration] = None,
                                ) extends ActorScheduler {
  export scheduler.*
  private given ActorRuntime = this

  val logger: Logger = makeLogger

  def spawn[T](factory: ActorFactory[T]): ActorRef[T] =
    factory.self

  override def scheduleMessage[T](delay: FiniteDuration, to: ActorRef[T], message: T): Cancellable =
    schedule(delay)(to.timerTell(message))

  override def scheduleMessage[T](delay: FiniteDuration, period: FiniteDuration, to: ActorRef[T], message: T): Cancellable =
    schedule(delay, period)(to.timerTell(message))

}
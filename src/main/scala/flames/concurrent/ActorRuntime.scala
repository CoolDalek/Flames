package flames.concurrent

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.ExecutionContext
import flames.util.Logger
import flames.util.Logger.LogLevel

import scala.collection.concurrent.TrieMap
import scala.collection.mutable

final class ActorRuntime private(
                                  makeLogger: => Logger,
                                  makeScheduler: => Scheduler,
                                  val autoYieldCount: Int,
                                  val autoYieldTime: Option[FiniteDuration],
                                ) extends ActorScheduler {
  private val logger = makeLogger
  private val scheduler = makeScheduler
  export scheduler.*
  private given ActorRuntime = this

  def spawn[T](factory: ActorFactory[T]): ActorRef[T] =
    factory.self

  override def scheduleMessage[T](delay: FiniteDuration, to: ActorRef[T], message: T): Cancellable =
    schedule(delay)(to.timerTell(message))

  override def scheduleMessage[T](delay: FiniteDuration, period: FiniteDuration, to: ActorRef[T], message: T): Cancellable =
    schedule(delay, period)(to.timerTell(message))

}
object ActorRuntime {

  def apply(logger: Logger,
            scheduler: Scheduler,
            autoYieldCount: Int = 8,
            autoYieldTime: Option[FiniteDuration] = None): ActorRuntime =
    new ActorRuntime(logger, scheduler, autoYieldCount, autoYieldTime)

  def default(logLevel: LogLevel): ActorRuntime = {
    lazy val runtime: ActorRuntime =
      new ActorRuntime(logger, scheduler, 8, None)
    lazy val logger: Logger = {
      given ActorRuntime = runtime
      new ActorLogger(logLevel)
    }
    lazy val scheduler: Scheduler =
      Scheduler.default(logger)
    runtime
  }

}
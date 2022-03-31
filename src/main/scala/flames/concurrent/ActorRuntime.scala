package flames.concurrent

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.ExecutionContext
import flames.util.Logger
import flames.util.Logger.LogLevel

import scala.collection.concurrent.TrieMap
import scala.collection.mutable

sealed trait ActorRuntime extends ActorScheduler {
  protected given ActorRuntime = this

  def autoYieldCount: Int

  def autoYieldTime: Option[FiniteDuration]

  def logger: Logger

  def spawn[T](factory: ActorFactory[T]): ActorRef[T] =
    factory.self

  override def scheduleMessage[T](delay: FiniteDuration, to: ActorRef[T], message: T): Cancellable =
    schedule(delay)(to.timerTell(message))

  override def scheduleMessage[T](delay: FiniteDuration, period: FiniteDuration, to: ActorRef[T], message: T): Cancellable =
    schedule(delay, period)(to.timerTell(message))

}
object ActorRuntime {

  val defaultYieldTime: Option[FiniteDuration] = None
  val defaultYieldCount: Int = 8

  private class SimpleRuntime(
                               val logger: Logger,
                               val autoYieldCount: Int,
                               val autoYieldTime: Option[FiniteDuration],
                               scheduler: Scheduler
                             ) extends ActorRuntime {
    export scheduler.*
  }

  def apply(logger: Logger,
            scheduler: Scheduler,
            autoYieldCount: Int = defaultYieldCount,
            autoYieldTime: Option[FiniteDuration] = defaultYieldTime): ActorRuntime =
    new SimpleRuntime(logger, autoYieldCount, autoYieldTime, scheduler)

  def default(logLevel: LogLevel): ActorRuntime =
    new ActorRuntime {
      val autoYieldCount: Int = defaultYieldCount

      val autoYieldTime: Option[FiniteDuration] = defaultYieldTime

      val logger: Logger = ActorLogger(logLevel)

      val scheduler: Scheduler = Scheduler.default(logger)
      
      export scheduler.*
    }


}
package flames.concurrent.actor

import flames.concurrent.*
import flames.concurrent.actor.fiber.*
import flames.logging.*
import flames.util.Id

import java.lang.Thread.UncaughtExceptionHandler
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger
import scala.collection.concurrent.TrieMap
import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

sealed trait ActorRuntime extends ActorScheduler {

  def logger: Logger

  private type Parent = ActorRef[Nothing] | Null

  private[actor] def makeBlocking[T](behavior: Behavior[T], parent: Parent): ActorFiber[T]

  private[actor] def makePinned[T](behavior: Behavior[T], parent: Parent): ActorFiber[T]

  private[actor] def makeAsync[T](behavior: Behavior[T], parent: Parent): ActorFiber[T]

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
    new SimpleRuntime(
      logger = logger,
      autoYieldCount = autoYieldCount,
      autoYieldTime = autoYieldTime,
      scheduler = scheduler,
    )

  def default(
               fiberConfig: FiberConfig = FiberConfig(),
               logLevel: LogLevel,
               timestampPattern: String = ActorLogger.defaultTimestampPattern,
               printer: Printer[Id] = ActorLogger.defaultPrinter,
             ): ActorRuntime =
    new ActorRuntime {

      override def makeBlocking[T](behavior: Behavior[T], parent: Parent): ActorFiber[T] = {
        val token = ActorToken()
        val state = FiberState.default(
          fiberConfig,
          parent,
          token,
        )
        val execution = ShiftedExecution(
          blockingEC,
          state
        )
        ActorFiber[T](
          state,
          execution,
          behavior,
          reporter,
        )
      }

      override def makePinned[T](behavior: Behavior[T], parent: Parent): ActorFiber[T] = ???

      override def makeAsync[T](behavior: Behavior[T], parent: Parent): ActorFiber[T] = ???

      val logger: Logger = RuntimeLogger(logLevel, timestampPattern, printer)

      private val reporter = Logger.asUncaughtExceptionHandler(logger)

      val scheduler: Scheduler = Scheduler.default(logger)

      export scheduler.*

    }


}
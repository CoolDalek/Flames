package flames.concurrent

import flames.logging.*

import java.lang.Thread.UncaughtExceptionHandler
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.ExecutionContext
import scala.collection.concurrent.TrieMap
import scala.collection.mutable
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

sealed trait ActorRuntime extends ActorScheduler {
  protected given ActorRuntime = this

  def autoYieldCount: Int

  def autoYieldTime: Option[FiniteDuration]

  def logger: Logger

  protected def pinnedActorThreadPool: PinnedActorThreadPool

  def spawn[T](factory: ActorFactory[T]): ActorRef[T] =
    factory.self

  private[concurrent] def runPinned(run: => Unit): Unit =
    pinnedActorThreadPool.acquireThread(run)

  private[concurrent] def watchExternalPinned(): Unit =
    pinnedActorThreadPool.watchExternal()

  private[concurrent] def forgetExternalPinned(): Unit =
    pinnedActorThreadPool.forgetExternal()

  override def scheduleMessage[T](delay: FiniteDuration, to: ActorRef[T], message: T): Cancellable =
    schedule(delay)(to.timerTell(message))

  override def scheduleMessage[T](delay: FiniteDuration, period: FiniteDuration, to: ActorRef[T], message: T): Cancellable =
    schedule(delay, period)(to.timerTell(message))

}
object ActorRuntime {

  inline private val pinned = "Pinned"

  val defaultYieldTime: Option[FiniteDuration] = None
  val defaultYieldCount: Int = 8
  def defaultPinnedActorThreadFactory(reporter: UncaughtExceptionHandler): ThreadFactory =
    DefaultThreadFactory(pinned, reporter)
  def defaultPinnedActorThreadFactory(logger: Logger): ThreadFactory =
    defaultPinnedActorThreadFactory(Logger.asUncaughtExceptionHandler(logger))

  private class SimpleRuntime(
                               val logger: Logger,
                               val autoYieldCount: Int,
                               val autoYieldTime: Option[FiniteDuration],
                               val pinnedActorThreadPool: PinnedActorThreadPool,
                               scheduler: Scheduler
                             ) extends ActorRuntime {
    export scheduler.{shutdown as _, *}

    override def shutdown(): Unit = {
      scheduler.shutdown()
      pinnedActorThreadPool.shutdown()
    }
  }

  def apply(logger: Logger,
            scheduler: Scheduler,
            pinnedActorThreadPool: PinnedActorThreadPool,
            autoYieldCount: Int = defaultYieldCount,
            autoYieldTime: Option[FiniteDuration] = defaultYieldTime): ActorRuntime =
    new SimpleRuntime(
      logger = logger,
      autoYieldCount = autoYieldCount,
      autoYieldTime = autoYieldTime,
      pinnedActorThreadPool = pinnedActorThreadPool,
      scheduler = scheduler,
    )

  def default(logLevel: LogLevel): ActorRuntime =
    new ActorRuntime {
      val autoYieldCount: Int = defaultYieldCount

      val autoYieldTime: Option[FiniteDuration] = defaultYieldTime

      val logger: Logger = RuntimeLogger(logLevel)

      val pinnedActorThreadPool: PinnedActorThreadPool =
        PinnedActorThreadPool(
          0,
          Scheduler.availableProcessors,
          Scheduler.defaultKeepAlive,
          DefaultThreadFactory(
            pinned,
            Logger.asUncaughtExceptionHandler(logger)
          ),
        )
        
      watchExternalPinned()

      val scheduler: Scheduler = Scheduler.default(logger)

      export scheduler.{shutdown as _, *}

      override def shutdown(): Unit = {
        scheduler.shutdown()
        pinnedActorThreadPool.shutdown()
      }

    }


}
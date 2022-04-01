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

  protected def pinnedActorThreadFactory: ThreadFactory

  def spawn[T](factory: ActorFactory[T]): ActorRef[T] =
    factory.self

  private[concurrent] def pinnedThread[T](run: => T): Thread =
    pinnedActorThreadFactory.newThread(() => run)

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
                               val pinnedActorThreadFactory: ThreadFactory,
                               scheduler: Scheduler
                             ) extends ActorRuntime {
    export scheduler.*
  }

  def apply(logger: Logger,
            scheduler: Scheduler,
            pinnedActorThreadFactory: ThreadFactory,
            autoYieldCount: Int = defaultYieldCount,
            autoYieldTime: Option[FiniteDuration] = defaultYieldTime): ActorRuntime =
    new SimpleRuntime(
      logger = logger,
      autoYieldCount = autoYieldCount,
      autoYieldTime = autoYieldTime,
      pinnedActorThreadFactory = pinnedActorThreadFactory,
      scheduler = scheduler,
    )

  def default(logLevel: LogLevel): ActorRuntime =
    new ActorRuntime {
      val autoYieldCount: Int = defaultYieldCount

      val autoYieldTime: Option[FiniteDuration] = defaultYieldTime

      var _pinnedActorThreadFactory: ThreadFactory =
        defaultPinnedActorThreadFactory { (t, e) =>
          println(s"Exception in thread ${t.getName}")
          e.printStackTrace()
        }

      val logger: Logger = ActorLogger(logLevel)

      _pinnedActorThreadFactory = DefaultThreadFactory(
        namePrefix = pinned,
        reporter = Logger.asUncaughtExceptionHandler(logger),
        initCount = 1,
      )

      override def pinnedActorThreadFactory: ThreadFactory = _pinnedActorThreadFactory

      val scheduler: Scheduler = Scheduler.default(logger)

      export scheduler.*

    }


}
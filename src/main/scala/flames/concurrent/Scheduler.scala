package flames.concurrent

import flames.logging.Logger

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.*
import scala.util.chaining.*
import java.lang.Thread.UncaughtExceptionHandler
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.*

trait Scheduler extends ExecutionContext with Shutdown {

  inline def execute(inline action: => Unit): Unit = execute(() => action)
  
  def blocking(action: => Unit): Unit

  def schedule[T](delay: FiniteDuration)(action: => T): Cancellable

  def schedule[T](delay: FiniteDuration, period: FiniteDuration)(action: => T): Cancellable

}
object Scheduler {
  val availableProcessors: Int = sys.runtime.availableProcessors
  val defaultKeepAlive: FiniteDuration = 60.seconds

  def default(
               logger: Logger,
               minBlockingThreads: Int = 0,
               maxBlockingThreads: Int = Int.MaxValue,
               minComputeThreads: Int = availableProcessors,
               maxComputeThreads: Int = availableProcessors,
               timerThreads: Int = 1,
               keepAlive: FiniteDuration = defaultKeepAlive,
               interruptOnCancel: Boolean = false,
             ): Scheduler = new Scheduler {

    private val reporter = Logger.asUncaughtExceptionHandler(logger)

    private def factory(poolName: String, priority: Int = Thread.NORM_PRIORITY): DefaultThreadFactory =
      DefaultThreadFactory(poolName, reporter, priority)

    private val timer =
      ScheduledThreadPoolExecutor(
        timerThreads,
        factory("Timer", Thread.MAX_PRIORITY),
      ).tap { pool =>
        pool.setKeepAliveTime(keepAlive.length, keepAlive.unit)
        pool.setRemoveOnCancelPolicy(true)
      }

    private val blocker =
      ThreadPoolExecutor(
        minBlockingThreads,
        maxBlockingThreads,
        keepAlive.length,
        keepAlive.unit,
        SynchronousQueue[Runnable](),
        factory("Blocking"),
      )

    private val compute =
      new ForkJoinPool(
        minComputeThreads,
        factory("Compute"),
        reporter,
        false,
        minComputeThreads,
        maxComputeThreads,
        1,
        null,
        keepAlive.length,
        keepAlive.unit,
      )

    override def blocking(action: => Unit): Unit =
      blocker.execute(() => action)

    private def cancellable(scheduled: ScheduledFuture[_]): Cancellable =
      new Cancellable {
        override def isCancelled: Boolean = scheduled.isCancelled

        override def cancel(): Boolean = scheduled.cancel(interruptOnCancel)
      }

    override def schedule[T](delay: FiniteDuration)(action: => T): Cancellable =
      cancellable {
        timer.schedule(
          new Runnable {
            override def run(): Unit = action
          },
          delay.toNanos,
          TimeUnit.NANOSECONDS,
        )
      }

    override def schedule[T](delay: FiniteDuration, period: FiniteDuration)(action: => T): Cancellable =
      cancellable {
        timer.scheduleWithFixedDelay(
          () => action,
          delay.toNanos,
          period.toNanos,
          TimeUnit.NANOSECONDS,
        )
      }

    override def shutdown(): Unit = {
      timer.shutdown()
      compute.shutdown()
      blocker.shutdown()
    }

    override def execute(runnable: Runnable): Unit =
      compute.execute(runnable)

    override def reportFailure(cause: Throwable): Unit =
      logger.error(cause)

  }

}
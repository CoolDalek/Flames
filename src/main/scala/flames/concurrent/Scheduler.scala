package flames.concurrent

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.*
import scala.util.chaining.*
import flames.util.Logger

import java.lang.Thread.UncaughtExceptionHandler
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.*

trait Scheduler extends ExecutionContext with AutoCloseable {

  inline def execute(inline action: => Unit): Unit = execute(() => action)
  
  def blocking(action: => Unit): Unit

  def schedule[T](delay: FiniteDuration)(action: => T): Cancellable

  def schedule[T](delay: FiniteDuration, period: FiniteDuration)(action: => T): Cancellable

  override def close(): Unit = shutdown()

  def shutdown(): Unit

}
object Scheduler {

  private val availableProcessors = sys.runtime.availableProcessors

  def default(
               logger: Logger,
               minBlockingThreads: Int = 0,
               maxBlockingThreads: Int = Int.MaxValue,
               minComputeThreads: Int = availableProcessors,
               maxComputeThreads: Int = availableProcessors,
               timerThreads: Int = 1,
               keepAlive: FiniteDuration = 60.seconds,
               interruptOnCancel: Boolean = false,
             ): Scheduler = new Scheduler {

    private val reporter = new UncaughtExceptionHandler {
      override def uncaughtException(t: Thread, e: Throwable): Unit =
        logger.error(e, s"Unexpected exception in thread ${t.getName}.")
    }

    private class Factory(poolName: String, priority: Int) extends ForkJoinPool.ForkJoinWorkerThreadFactory with ThreadFactory {
      private val count = AtomicInteger(0)

      private def configure[T <: Thread](instance: T): T = {
        instance.setName(s"$poolName-${count.getAndIncrement}")
        instance.setDaemon(true)
        instance.setUncaughtExceptionHandler(reporter)
        instance.setPriority(priority)
        instance
      }

      private class FjkThread(pool: ForkJoinPool) extends ForkJoinWorkerThread(pool)

      override def newThread(pool: ForkJoinPool): ForkJoinWorkerThread =
        configure(FjkThread(pool))

      override def newThread(r: Runnable): Thread =
        configure(Thread())
    }

    private val timer =
      ScheduledThreadPoolExecutor(
        timerThreads,
        Factory("Timer", Thread.MAX_PRIORITY),
      ).tap {
        _.setKeepAliveTime(keepAlive.length, keepAlive.unit)
      }

    private val blocker =
      ThreadPoolExecutor(
        minBlockingThreads,
        maxBlockingThreads,
        keepAlive.length,
        keepAlive.unit,
        SynchronousQueue[Runnable](),
        Factory("Blocking", Thread.NORM_PRIORITY),
      )

    private val compute =
      new ForkJoinPool(
        minComputeThreads,
        Factory("Compute", Thread.NORM_PRIORITY),
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

    private def schedule[T](delay: Long, period: Long, action: => T): Cancellable = {
      val future = timer.scheduleWithFixedDelay(
        () => action,
        delay,
        period,
        TimeUnit.NANOSECONDS,
      )
      new Cancellable {
        override def isCancelled: Boolean = future.isCancelled

        override def cancel(): Boolean = future.cancel(interruptOnCancel)
      }
    }

    override def schedule[T](delay: FiniteDuration)(action: => T): Cancellable =
      schedule(delay.toNanos, 0, action)

    override def schedule[T](delay: FiniteDuration, period: FiniteDuration)(action: => T): Cancellable =
      schedule(delay.toNanos, period.toNanos, action)

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
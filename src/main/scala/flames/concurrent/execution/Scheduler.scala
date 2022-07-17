package flames.concurrent.execution

import flames.logging.Logger

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.*
import scala.util.chaining.*
import java.lang.Thread.UncaughtExceptionHandler
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.*
import scala.annotation.threadUnsafe

trait Scheduler extends ExecutionContext with Shutdown { self =>

  inline def execute(inline action: => Unit): Unit = execute(() => action)
  
  def blocking(action: Runnable): Unit
  
  inline def blocking(inline action: => Unit): Unit = blocking(() => action)

  @threadUnsafe
  lazy val blockingEC: ExecutionContext = new ExecutionContext {
    
    override def execute(runnable: Runnable): Unit = self.blocking(runnable)

    override def reportFailure(cause: Throwable): Unit = self.reportFailure(cause)
    
  }

  def schedule[T](delay: FiniteDuration)(action: => T): Cancellable

  def schedule[T](delay: FiniteDuration, period: FiniteDuration)(action: => T): Cancellable

  def config: SchedulerConfig

}
object Scheduler {

  private class Default(
                         logger: Logger,
                         val config: SchedulerConfig,
                       ) extends Scheduler {
    import config.*

    override def reportFailure(exc: Throwable): Unit =
      logger.error(exc,"Unexpected exception in scheduler, most likely on CPU-bound thread.")

    private def factory(poolName: String, priority: Int = Thread.NORM_PRIORITY): DefaultThreadFactory =
      DefaultThreadFactory(poolName, logger, priority)

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
        logger,
        false,
        minComputeThreads,
        maxComputeThreads,
        1,
        null,
        keepAlive.length,
        keepAlive.unit,
      )

    override def blocking(action: Runnable): Unit =
      blocker.execute(action)

    private def cancellable(scheduled: ScheduledFuture[?]): Cancellable =
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

  }

  def default(
               logger: Logger,
               config: SchedulerConfig = SchedulerConfig.default,
             ): Scheduler =
    new Default(logger, config)

}
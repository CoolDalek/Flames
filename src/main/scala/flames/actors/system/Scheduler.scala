package flames.actors.system

import java.util.concurrent.{ScheduledFuture, ScheduledThreadPoolExecutor, TimeUnit}
import scala.concurrent.duration.FiniteDuration

trait Scheduler {

  def delayed(delay: FiniteDuration)(runnable: Runnable): Cancellable

  def withFixedDelay(delay: FiniteDuration, period: FiniteDuration)(
    runnable: Runnable,
  ): Cancellable

  def withFixedDelay(period: FiniteDuration)(runnable: Runnable): Cancellable =
    withFixedDelay(period, period)(runnable)

  def atFixedRate(delay: FiniteDuration, period: FiniteDuration)(
    runnable: Runnable,
  ): Cancellable

  def atFixedRate(period: FiniteDuration)(runnable: Runnable): Cancellable =
    atFixedRate(period, period)(runnable)

}
object Scheduler {

  def java(
    prefix: String,
    interruptRunning: Boolean,
    reporter: Thread.UncaughtExceptionHandler,
  ): Scheduler = java(
    ScheduledThreadPoolExecutor(
      1,
      CountingThreadFactory(
        prefix,
        Thread.MAX_PRIORITY,
        reporter,
      ),
    ),
    interruptRunning,
  )

  def java(
    underlying: ScheduledThreadPoolExecutor,
    interruptRunning: Boolean,
  ): Scheduler = new:

    private inline def cancellable(inline
    schedule: ScheduledThreadPoolExecutor => ScheduledFuture[?],
    ): Cancellable = Cancellable.javaFuture(schedule(underlying), interruptRunning)

    override def delayed(delay: FiniteDuration)(runnable: Runnable): Cancellable =
      cancellable(_.schedule(runnable, delay.length, delay.unit))

    override def withFixedDelay(delay: FiniteDuration, period: FiniteDuration)(
      runnable: Runnable,
    ): Cancellable =
      cancellable(
        _.scheduleWithFixedDelay(
          runnable,
          delay.toNanos,
          period.toNanos,
          TimeUnit.NANOSECONDS,
        ),
      )

    override def atFixedRate(delay: FiniteDuration, period: FiniteDuration)(
      runnable: Runnable,
    ): Cancellable =
      cancellable(
        _.scheduleAtFixedRate(
          runnable,
          delay.toNanos,
          period.toNanos,
          TimeUnit.NANOSECONDS,
        ),
      )

  end java

}

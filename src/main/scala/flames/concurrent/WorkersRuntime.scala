package flames.concurrent

import java.util.{Timer, TimerTask}
import java.util.concurrent.Executor
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

final class WorkersRuntime private(
                                    ec: ExecutionContext,
                                    timer: Timer,
                                    reporter: FailureReporter,
                                  ) extends ExecutionContext with AutoCloseable {
  export ec.execute
  export reporter.reportFailure

  inline def execute[T](action: => T): Unit = ec.execute(() => action)

  override def close(): Unit = {
    timer.cancel()
  }

  trait Scheduled extends TimerTask with Cancellable {

    @volatile private var cancelled = false

    override def isCancelled: Boolean = cancelled

    override def cancel(): Boolean = {
      val result = super.cancel()
      cancelled = result
      result
    }

  }

  private inline def schedule[T](action: => T)(how: TimerTask => Unit): Cancellable = {
    val task: Scheduled = () => action
    how(task)
    task
  }

  def schedule[T](delay: FiniteDuration)(action: => T): Cancellable =
    schedule(action) { task =>
      timer.schedule(task, delay.toMillis)
    }
  
  def schedule[T](delay: FiniteDuration, period: FiniteDuration)(action: => T): Cancellable =
    schedule(action) { task =>
      timer.schedule(task, delay.toMillis, period.toMillis)
    }
  
}
object WorkersRuntime {
  import ExecutionContext.*

  private inline def defaultExecutor: Executor = null.asInstanceOf[Executor]
  private val defaultReporter: FailureReporter = _.printStackTrace()
  private def defaultTimer: Timer = Timer(true)

  def apply(): WorkersRuntime =
   new WorkersRuntime(
     fromExecutor(defaultExecutor),
     defaultTimer,
     defaultReporter,
   )

  def apply(reporter: FailureReporter): WorkersRuntime =
    new WorkersRuntime(
      fromExecutor(defaultExecutor, reporter),
      defaultTimer,
      reporter,
    )

  def apply(timer: Timer): WorkersRuntime =
    new WorkersRuntime(
      fromExecutor(defaultExecutor),
      timer,
      defaultReporter,
    )

  def apply(ec: ExecutionContext): WorkersRuntime =
    new WorkersRuntime(
      ec,
      defaultTimer,
      ec.reportFailure,
    )

  def apply(reporter: FailureReporter, timer: Timer): WorkersRuntime =
    new WorkersRuntime(
      fromExecutor(defaultExecutor, reporter),
      timer,
      reporter,
    )

  def apply(ec: ExecutionContext, timer: Timer): WorkersRuntime =
    new WorkersRuntime(
      ec,
      timer,
      ec.reportFailure,
    )

}
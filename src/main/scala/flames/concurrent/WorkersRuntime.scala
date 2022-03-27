package flames.concurrent

import flames.util.FailureReporter

import java.util.{Timer, TimerTask}
import java.util.concurrent.Executor
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

final class WorkersRuntime private(
                                    executionContext: ExecutionContext,
                                    timer: Timer,
                                    reporter: FailureReporter,
                                    executorFactory: WorkerExecutor.Factory,
                                  ) extends ExecutionContext with AutoCloseable with FailureReporter {
  export executionContext.execute
  export reporter.reportFailure

  inline def execute[T](inline action: => T): Unit = executionContext.execute(() => action)

  override def close(): Unit = {
    timer.cancel()
  }

  trait Scheduled extends TimerTask with Cancellable {

    @volatile private var cancelled = false

    override def isCancelled: Boolean = cancelled

    override def cancel(): Boolean = {
      val result = super.cancel()
      cancelled = result || cancelled
      result
    }

  }

  inline private def schedule[T](action: => T)(inline how: TimerTask => Unit): Cancellable = {
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

  private[concurrent] def makeExecutor[T](act: Behavior[T]): WorkerExecutor[T] =
    executorFactory[T](act, this)
  
}
object WorkersRuntime {
  import ExecutionContext.*
  import WorkerExecutor.*

  private inline def defaultExecutor: Executor = null.asInstanceOf[Executor]
  private val defaultReporter: FailureReporter = _.printStackTrace()
  private def defaultTimer: Timer = Timer(true)

  def apply(): WorkersRuntime =
   new WorkersRuntime(
     fromExecutor(defaultExecutor),
     defaultTimer,
     defaultReporter,
     defaultFactory,
   )

  def apply(executorFactory: WorkerExecutor.Factory): WorkersRuntime =
    new WorkersRuntime(
      fromExecutor(defaultExecutor),
      defaultTimer,
      defaultReporter,
      executorFactory,
    )

  def apply(reporter: FailureReporter): WorkersRuntime =
    new WorkersRuntime(
      fromExecutor(defaultExecutor, reporter.reportFailure),
      defaultTimer,
      reporter,
      defaultFactory,
    )

  def apply(reporter: FailureReporter, executorFactory: WorkerExecutor.Factory): WorkersRuntime =
    new WorkersRuntime(
      fromExecutor(defaultExecutor, reporter.reportFailure),
      defaultTimer,
      reporter,
      executorFactory,
    )

  def apply(timer: Timer): WorkersRuntime =
    new WorkersRuntime(
      fromExecutor(defaultExecutor),
      timer,
      defaultReporter,
      defaultFactory,
    )

  def apply(timer: Timer, executorFactory: WorkerExecutor.Factory): WorkersRuntime =
    new WorkersRuntime(
      fromExecutor(defaultExecutor),
      timer,
      defaultReporter,
      executorFactory,
    )

  def apply(executionContext: ExecutionContext): WorkersRuntime =
    new WorkersRuntime(
      executionContext,
      defaultTimer,
      executionContext.reportFailure,
      defaultFactory,
    )

  def apply(executionContext: ExecutionContext, executorFactory: WorkerExecutor.Factory): WorkersRuntime =
    new WorkersRuntime(
      executionContext,
      defaultTimer,
      executionContext.reportFailure,
      executorFactory,
    )

  def apply(reporter: FailureReporter, timer: Timer): WorkersRuntime =
    new WorkersRuntime(
      fromExecutor(defaultExecutor, reporter.reportFailure),
      timer,
      reporter,
      defaultFactory,
    )

  def apply(reporter: FailureReporter, timer: Timer, executorFactory: WorkerExecutor.Factory): WorkersRuntime =
    new WorkersRuntime(
      fromExecutor(defaultExecutor, reporter.reportFailure),
      timer,
      reporter,
      executorFactory,
    )

  def apply(executionContext: ExecutionContext, timer: Timer): WorkersRuntime =
    new WorkersRuntime(
      executionContext,
      timer,
      executionContext.reportFailure,
      defaultFactory,
    )

  def apply(executionContext: ExecutionContext, timer: Timer, executorFactory: WorkerExecutor.Factory): WorkersRuntime =
    new WorkersRuntime(
      executionContext,
      timer,
      executionContext.reportFailure,
      executorFactory,
    )

}
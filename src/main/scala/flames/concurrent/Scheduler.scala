package flames.concurrent

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

trait Scheduler extends ExecutionContext with AutoCloseable {

  inline def execute(inline action: => Unit): Unit = execute(() => action)

  def schedule[T](delay: FiniteDuration)(action: => T): Cancellable

  def schedule[T](delay: FiniteDuration, period: FiniteDuration)(action: => T): Cancellable

}
package flames.logging

import flames.util.Show
import sourcecode.{Enclosing, Line}

@FunctionalInterface
trait FailureReporter {

  def reportFailure[T: Show](exc: Throwable, ctx: => T)(using Enclosing, Line): Unit

}
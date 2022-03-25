package flames.util

@FunctionalInterface
trait FailureReporter {
  
  def reportFailure(exc: Throwable): Unit

}
package flames.concurrent

trait FailureReporter extends (Throwable => Unit) {
  
  final def apply(exc: Throwable): Unit = reportFailure(exc)
  
  def reportFailure(exc: Throwable): Unit

}
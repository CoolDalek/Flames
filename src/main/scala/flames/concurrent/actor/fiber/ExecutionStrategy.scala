package flames.concurrent.actor.fiber

trait ExecutionStrategy extends Runnable {
  
  def sleep(): Unit

  def `yield`(): Unit

  def continue(): Unit
  
}
object ExecutionStrategy {
  type Continuation = Runnable
  type Factory = Continuation => ExecutionStrategy
}
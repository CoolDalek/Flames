package flames.concurrent.actor.fiber

trait ExecutionStrategy {
  
  def sleep(continuation: => Unit): Unit

  def `yield`(continuation: => Unit): Unit

  def continue(continuation: => Unit): Unit
  
}
package flames.concurrent.execution

import flames.concurrent.execution.Execution.Continuation

trait ConcurrentRuntime {
  
  def atomic[T](init: T): Atomic[T]
  
  def execution(continuation: Continuation): Execution

}

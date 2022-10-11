package flames.concurrent.execution

import flames.concurrent.execution.Execution.Continuation
import flames.concurrent.execution.atomic.Atomic

trait ConcurrentRuntime {
  
  def atomic[T](init: T): Atomic[T]
  
  def execution(continuation: Continuation): Execution

}

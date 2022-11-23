package flames.concurrent.execution

import flames.concurrent.execution.atomic.AtomicFactory

trait ConcurrentRuntime:
  
  def currentFiber: Fiber[Nothing] | Null
  
  def syncRequired(): Boolean
  
  def atomicFactory: AtomicFactory

end ConcurrentRuntime

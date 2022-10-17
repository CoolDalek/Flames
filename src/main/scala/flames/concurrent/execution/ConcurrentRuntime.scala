package flames.concurrent.execution

import flames.concurrent.execution.atomic.AtomicFactory

trait ConcurrentRuntime:
  
  def syncRequired(): Boolean
  
  def executeNext(using TimeStealer): Unit
  
  def atomicFactory: AtomicFactory

end ConcurrentRuntime

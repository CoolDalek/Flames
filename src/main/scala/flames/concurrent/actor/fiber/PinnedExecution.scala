package flames.concurrent.actor.fiber

import flames.concurrent.execution.ProcessState.*
import ExecutionStrategy.*

class PinnedExecution[T](
                          continuation: Continuation,
                          state: FiberState[T],
                        ) extends ExecutionStrategy {
  import state.procState

  override def run(): Unit =
    continuation.run()

  override def sleep(): Unit = {
    procState.set(Idle)
    if (state.hasMessage) procState.set(Running)
    else synchronized(wait())
    state.prepare()
  }

  override def `yield`(): Unit = {
    Thread.`yield`()
    state.prepare()
  }

  override def continue(): Unit =
    if (procState.compareAndSet(Idle, Running)) {
      synchronized(notify())
    }

}
object PinnedExecution {
  
  def apply[T](state: FiberState[T]): Factory =
    cont => new PinnedExecution[T](cont, state)
  
}
package flames.concurrent.actor.fiber

import flames.concurrent.execution.ProcessState.*
import ExecutionStrategy.*
import PinnedExecution.*

class PinnedExecution[T](
                          runner: Runner,
                          continuation: Continuation,
                          state: FiberState[T],
                        ) extends ExecutionStrategy {
  import state.procState

  override def run(): Unit =
    runner(continuation)

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
  type Runner = Runnable => Unit
  
  def apply[T](runner: Runner, state: FiberState[T]): Factory =
    cont => new PinnedExecution[T](runner, cont, state)
  
}
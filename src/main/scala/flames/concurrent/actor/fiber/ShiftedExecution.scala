package flames.concurrent.actor.fiber

import flames.concurrent.ProcessState.*

import scala.concurrent.ExecutionContext

class ShiftedExecution[T](ec: ExecutionContext, state: FiberState[T]) extends ExecutionStrategy {
  import state.procState

  private def run(continuation: => Unit): Unit =
    ec.execute(() => continuation)

  override def sleep(continuation: => Unit): Unit = {
    state.loop = false
    procState.set(Idle)
    if (state.hasMessage) continue(continuation)
  }

  override def `yield`(continuation: => Unit): Unit = {
    state.loop = false
    if(procState.compareAndSet(Running, Running)) run(continuation)
  }

  override def continue(continuation: => Unit): Unit =
    if (procState.compareAndSet(Idle, Running)) run(continuation)

}
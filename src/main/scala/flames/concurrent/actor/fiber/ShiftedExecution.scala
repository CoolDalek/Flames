package flames.concurrent.actor.fiber

import flames.concurrent.execution.ProcessState.*

import scala.concurrent.ExecutionContext
import ExecutionStrategy.*

class ShiftedExecution[T](
                           continuation: Continuation,
                           ec: ExecutionContext,
                           state: FiberState[T],
                         ) extends ExecutionStrategy {
  import state.procState

  override def run(): Unit =
    ec.execute(continuation)

  override def sleep(): Unit = {
    state.loop = false
    procState.set(Idle)
    if (state.hasMessage) continue()
  }

  override def `yield`(): Unit = {
    state.loop = false
    if(procState.compareAndSet(Running, Running)) run()
  }

  override def continue(): Unit =
    if (procState.compareAndSet(Idle, Running)) run()

}
object ShiftedExecution {

  def apply[T](ec: ExecutionContext, state: FiberState[T]): Factory =
    cont => new ShiftedExecution[T](cont, ec, state)

}
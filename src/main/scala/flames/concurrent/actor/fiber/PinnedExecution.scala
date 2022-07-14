package flames.concurrent.actor.fiber

import flames.concurrent.ProcessState.*

class PinnedExecution[T](state: FiberState[T]) extends ExecutionStrategy with Runnable {
  import state.procState

  private type Cont = () => Unit
  private var cont: Cont = null.asInstanceOf[Cont]

  override def run(): Unit = {
    synchronized(wait())
    cont()
  }

  override def sleep(continuation: => Unit): Unit = {
    procState.set(Idle)
    if (state.hasMessage) procState.set(Running)
    else synchronized(wait())
    state.prepare()
  }

  override def `yield`(continuation: => Unit): Unit = {
    Thread.`yield`()
    state.prepare()
  }

  override def continue(continuation: => Unit): Unit =
    if (procState.compareAndSet(Idle, Running)) {
      cont = () => continuation
      synchronized(notify())
    }

}

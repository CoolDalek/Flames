package flames.concurrent

import ProcessState.*

abstract class ShiftedFiber[T](
                                r: ActorRuntime,
                                b: Behavior[T],
                              ) extends ActorFiber[T](r, b) {

  final override protected def trySleep(): Unit = {
    loop = false
    state.set(Idle)
    if (hasMessage) continue()
  }

  final override protected def yieldExecution(): Unit = {
    loop = false
    run()
  }

  final override protected def continue(): Unit =
    if (state.compareAndSet(Idle, Running)) run()

}
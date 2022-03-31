package flames.concurrent

private[concurrent] abstract class ShiftedFiber[T](
                                                    r: ActorRuntime,
                                                    b: Behavior[T],
                                                  ) extends ActorFiber[T](r, b) {

  final override protected def trySleep(): Unit = {
    state.set(FiberState.Idle)
    if (hasMessage) continue()
    loop = false
  }

  final override protected def yieldExecution(): Unit = {
    run()
    loop = false
  }

  final override protected def continue(): Unit = {
    import FiberState.*
    if (state.compareAndSet(Idle, Running)) run()
  }

}
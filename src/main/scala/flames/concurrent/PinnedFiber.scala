package flames.concurrent

private[concurrent] final class PinnedFiber[T](
                                                _runtime: ActorRuntime,
                                                behavior: Behavior[T],
                                              ) extends ActorFiber[T](_runtime, behavior) {
  runtime.pinnedThread(run()).start()

  override protected def run(): Unit = {
    state.set(FiberState.Running)
    executionLoop()
  }

  override protected def yieldExecution(): Unit = {
    prepare()
    Thread.`yield`()
  }

  override protected def continue(): Unit =
    synchronized {
      if(state.compareAndSet(FiberState.Idle, FiberState.Running)) notify()
    }

  override protected def trySleep(): Unit = {
    state.set(FiberState.Idle)
    if (hasMessage) {
      state.set(FiberState.Running)
      prepare()
    } else synchronized {
      wait()
      prepare()
    }
  }

}
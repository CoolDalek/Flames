package flames.concurrent

private[concurrent] final class PinnedFiber[T](
                                                _runtime: ActorRuntime,
                                                behavior: Behavior[T],
                                              ) extends ActorFiber[T](_runtime, behavior) {
  private val pinned = runtime.pinnedThread {
    state.set(FiberState.Running)
    run()
  }

  override protected def run(): Unit = executionLoop()

  override protected def yieldExecution(): Unit = {
    prepare()
    Thread.`yield`()
  }

  override protected def continue(): Unit = ()

  override protected def trySleep(): Unit =
    if(hasMessage) prepare()
    else yieldExecution()

}
package flames.concurrent

private[concurrent] final class BlockingFiber[T](
                                                  _runtime: ActorRuntime,
                                                  behavior: Behavior[T],
                                                ) extends ShiftedFiber[T](_runtime, behavior) {
  override protected def run(): Unit =
    runtime.execute {
      executionLoop()
    }

}
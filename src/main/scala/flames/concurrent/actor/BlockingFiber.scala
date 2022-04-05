package flames.concurrent.actor

open class BlockingFiber[T](
                             _runtime: ActorRuntime,
                             behavior: Behavior[T],
                           ) extends ShiftedFiber[T](_runtime, behavior) {
  override protected def run(): Unit =
    runtime.blocking {
      executionLoop()
    }

}
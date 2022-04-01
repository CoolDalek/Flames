package flames.concurrent

import ProcessState.*
import PinnedFiber.*

private[concurrent] open class PinnedFiber[T](
                                               _runtime: ActorRuntime,
                                               behavior: Behavior[T],
                                               customThread: CustomThread,
                                             ) extends ActorFiber[T](_runtime, behavior) {

  protected[concurrent] def initialize(): Unit =
    customThread match {
      case null =>
        runtime.runPinned(run())
      case make: MakeThread =>
        runtime.watchExternalPinned()
        make { () =>
          run()
          runtime.forgetExternalPinned()
        }.start()
    }

  override protected def run(): Unit = {
    state.set(Running)
    executionLoop()
  }

  override protected def yieldExecution(): Unit = {
    prepare()
    Thread.`yield`()
  }

  override protected def continue(): Unit =
    if (state.compareAndSet(Idle, Running)) {
      synchronized(notify())
    }

  override protected def trySleep(): Unit = {
    state.set(Idle)
    if (hasMessage) {
      state.set(Running)
      prepare()
    } else synchronized {
      wait()
      prepare()
    }
  }

}
object PinnedFiber {
  type MakeThread = Runnable => Thread
  type CustomThread = MakeThread | Null
}
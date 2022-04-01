package flames.concurrent

import flames.concurrent.ActorRuntime
import flames.logging.{ActorLogger, LogEvent, LogLevel}

private[flames] final class RuntimeLogger(lvl: LogLevel)(using ActorRuntime) extends ActorLogger(lvl) {

  private def makeLoggerThread: PinnedFiber.MakeThread =
    (runnable: Runnable) => {
      val thread = new Thread(runnable, "Logger")
      thread.setDaemon(true)
      thread.setUncaughtExceptionHandler { (t, e) =>
        println("Exception in logger thread.")
        e.printStackTrace()
      }
      thread
    }

  final class LoggingFiber extends PinnedFiber(runtime, act(), null) {

    override protected[concurrent] def initialize(): Unit =
      makeLoggerThread { () =>
        run()
        runtime.forgetExternalPinned()
      }.start()

  }

  override protected[concurrent] lazy val fiber: ActorFiber[LogEvent] = {
    val logging = LoggingFiber()
    logging.initialize()
    logging
  }

}
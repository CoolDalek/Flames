package flames.concurrent.actor

import flames.concurrent.actor.RuntimeLogger.*
import flames.logging.{LogEvent, LogLevel, Printer}
import flames.util.Id

private[concurrent] final class RuntimeLogger(
                                               lvl: LogLevel,
                                               pattern: String,
                                               printer: Printer[Id]
                                             )(using ActorRuntime)
  extends ActorLogger(lvl, pattern, printer) with AnyActor[LogEvent] {

  override protected def makeFiber: ActorFiber[LogEvent] = new PinnedFiber(runtime, act(), null) {

    override protected[concurrent] def initialize(): PinnedFiber[LogEvent] = {
      makeLoggerThread { () =>
        safeRun()
      }.start()
      this
    }

  }

}
object RuntimeLogger {

  def makeLoggerThread: PinnedFiber.MakeThread =
    (runnable: Runnable) => {
      val thread = new Thread(runnable, "Logger")
      thread.setDaemon(true)
      thread.setUncaughtExceptionHandler { (t, e) =>
        println("Exception in logger thread.")
        e.printStackTrace()
      }
      thread
    }

}
package flames.concurrent.actor

import flames.concurrent.actor.RuntimeLogger.*
import flames.concurrent.actor.fiber.*
import flames.logging.{LogEvent, LogLevel, Printer}
import flames.util.Id

import scala.annotation.threadUnsafe

private[concurrent] final class RuntimeLogger(
                                               lvl: LogLevel,
                                               pattern: String,
                                               printer: Printer[Id]
                                             )(using ActorEnv)
  extends ActorLogger[ActorType.Pinned](lvl, pattern, printer) {

  @threadUnsafe
  override lazy val fiber: ActorFiber[LogEvent] = {
    val token = ActorToken()
    val state = FiberState.default[LogEvent](
      runtime.fiberConfig,
      null,
      token,
    )
    val executionFactory = PinnedExecution[LogEvent](state)
    val fiber = ActorFiber[LogEvent](
      state,
      act(),
      logError,
      executionFactory,
    )
    makeLoggerThread(fiber).start()
    fiber
  }

}
object RuntimeLogger {

  private def logError(exc: Throwable): Unit = {
    println("Exception in logger thread.")
    exc.printStackTrace()
  }

  private def makeLoggerThread(runnable: Runnable) =
      val thread = new Thread(runnable, "Logger")
      thread.setDaemon(true)
      thread.setUncaughtExceptionHandler { (_, exc) =>
        logError(exc)
      }
      thread
  end makeLoggerThread

}
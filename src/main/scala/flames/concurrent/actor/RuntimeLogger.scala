package flames.concurrent.actor

import flames.concurrent.actor.RuntimeLogger.*
import flames.concurrent.actor.fiber.*
import flames.logging.*
import flames.util.{Id, Show}
import flames.concurrent.execution.ExecutionModel
import sourcecode.{Enclosing, Line}

import scala.annotation.threadUnsafe

private[concurrent] final class RuntimeLogger(
                                               lvl: LogLevel,
                                               pattern: String,
                                               printer: Printer[Id]
                                             )(using ActorEnv)
  extends ActorLogger[ExecutionModel.Pinned](lvl, pattern, printer) {

  @threadUnsafe
  override lazy val fiber: ActorFiber[LogEvent] = {
    val path = runtime.pathFactory("logger", ActorParent.root)
    val state = FiberState.default[LogEvent](
      runtime.config.timerThreads,
      runtime.fiberConfig,
      ActorParent.root,
      path,
    )
    val executionFactory = PinnedExecution[LogEvent](state)
    val fiber = ActorFiber[LogEvent](
      state,
      act(),
      reporter,
      runtime,
      executionFactory,
    )
    makeLoggerThread(fiber).start()
    runtime.addRootChild(fiber.path, self)
    fiber
  }

}
object RuntimeLogger {

  private def reporter = new FailureReporter {
    override def reportFailure[T: Show](exc: Throwable, ctx: => T)(using Enclosing, Line): Unit = logError(exc)
  }

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
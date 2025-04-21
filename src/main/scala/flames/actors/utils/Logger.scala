package flames.actors.utils

import flames.actors.utils.Nulls.*

import java.io.{PrintStream, PrintWriter, StringWriter}
import java.time.{Clock, Instant}

// TODO: static vs dynamic contexts, encoders, layouts, TF, etc
trait Logger {

  def log(event: Logger.Event): Unit

}
object Logger {

  enum Level {
    case Trace
    case Debug
    case Warn
    case Error
    case Info
  }

  case class Event(
    level: Level,
    message: () => String | Null,
    error: Throwable | Null,
    thread: Thread,
    timestamp: Instant,
  )

  val DefaultFallback: Fallback = Fallback(
    System.err,
    Clock.systemUTC(),
  )

  class Fallback(
    out: PrintStream,
    clock: Clock,
  ) extends Logger, Thread.UncaughtExceptionHandler, (Throwable => Unit):

    override def log(event: Event): Unit = {
      val acc = StringWriter()
      val builder = PrintWriter(acc, true)
      builder.println(s"Fallback logger reports:")
      builder.println(s"\tLevel: ${event.level}")
      builder.println(s"\tTimestamp: ${event.timestamp}")
      builder.println(s"\tThread: ${event.thread.getName}")
      event.message.notNull { msg =>
        builder.println(s"\tMessage: ${msg}")
      }
      event.error.notNull { error =>
        builder.println("\tException:")
        error.printStackTrace(builder)
      }
      val result = acc.toString
      out.println(result)
      out.flush()
    }

    override def uncaughtException(thread: Thread, exc: Throwable): Unit =
      log(
        Event(
          Level.Error,
          null,
          exc,
          thread,
          clock.instant(),
        ),
      )

    override def apply(exc: Throwable): Unit =
      uncaughtException(Thread.currentThread(), exc)

  end Fallback

}
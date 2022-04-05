package flames.concurrent.actor

import flames.logging.*

import java.time.ZoneId
import java.time.format.DateTimeFormatter

import java.io.PrintWriter
import flames.util.{Id, Show, given}
import ActorLogger.given

trait ActorLogger(
                   override val logLevel: LogLevel,
                   timestampPattern: String,
                   printer: Printer[Id],
                 )(using ActorRuntime) extends Logger {
  this: AnyActor[LogEvent] =>

  override val timestampFormat: DateTimeFormatter =
    DateTimeFormatter.ofPattern(timestampPattern)
      .withZone(ZoneId.systemDefault)

  override def log(event: LogEvent): Unit = self.tell(event)

  override def act(): Behavior[LogEvent] = receive {
    case LogEvent(prefix, msg, exc) =>
      msg.notNull { value =>
        printer.println(s"$prefix $value")
      }
      exc.notNull { value =>
        printer.println(s"$prefix $value")
        val stackTrace: StackTrace = value.getStackTrace()
        stackTrace.notNull { value =>
          value.foreach(printer.println)
        }
      }
      same
  }

}
object ActorLogger {

  given Show[StackTraceElement] = Show.UnsafeShow

  val defaultTimestampPattern: String = "yyyy-MM-dd HH:mm:ss.SSS"

  val defaultPrinter: Printer[Id] = new Printer[Id] {
    override def print[T: Show](obj: T): Id[Unit] =
      Console.print(Show[T].show(obj))

    override def println[T: Show](obj: T): Id[Unit] =
      Console.println(Show[T].show(obj))
  }

  def default(lvl: LogLevel,
              pattern: String = defaultTimestampPattern,
              printer: Printer[Id] = defaultPrinter,
              customThread: PinnedFiber.CustomThread = PinnedFiber.defaultThread)
             (using ActorRuntime): ActorLogger =
    new ActorLogger(lvl, pattern, printer) with PinnedActor[LogEvent](customThread) {}

}
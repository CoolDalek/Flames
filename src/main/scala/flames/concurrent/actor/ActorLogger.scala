package flames.concurrent.actor

import flames.logging.*

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.io.PrintWriter
import flames.util.*
import flames.util.given
import flames.util.Nullable.*
import ActorLogger.given
import flames.concurrent.actor.behavior.Behavior
import flames.concurrent.execution.ExecutionModel

trait ActorLogger[Model <: ExecutionModel](
                                            override val logLevel: LogLevel, 
                                            timestampPattern: String, 
                                            printer: Printer[Id],
                                          ) extends Actor[LogEvent, Model] with Logger {


  override protected val name: String = "logger"

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
  }.ignoreSystem

}
object ActorLogger {

  given Show[StackTraceElement] = Show.unsafe.instance

  val defaultTimestampPattern: String = "yyyy-MM-dd HH:mm:ss.SSS"

  val defaultPrinter: Printer[Id] = new Printer[Id] {
    override def print[T: Show](obj: T): Id[Unit] =
      Console.print(Show[T].show(obj))

    override def println[T: Show](obj: T): Id[Unit] =
      Console.println(Show[T].show(obj))
  }

  def default(
               lvl: LogLevel,
               pattern: String = defaultTimestampPattern,
               printer: Printer[Id] = defaultPrinter,
             )(using ActorEnv): ActorLogger[ExecutionModel.Pinned] =
    new ActorLogger[ExecutionModel.Pinned](lvl, pattern, printer) {}

}
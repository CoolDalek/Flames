package flames.logging

import flames.concurrent.{ActorRuntime, Behavior, BlockingActor}
import flames.logging.Logger

import java.time.ZoneId
import java.time.format.DateTimeFormatter

final class ActorLogger(override val logLevel: LogLevel)
                       (using ActorRuntime) extends BlockingActor[LogEvent] with Logger {

  override val timestampFormat: DateTimeFormatter =
    DateTimeFormatter.ofPattern(
      "yyyy-MM-dd HH:mm:ss.SSS"
    ).withZone(ZoneId.systemDefault)

  override def log(event: LogEvent): Unit = self.tell(event)

  override def act(): Behavior[LogEvent] = receive {
    case LogEvent(prefix, msg, exc) =>
      msg.foreach { value =>
        println(s"$prefix $value")
      }
      exc.foreach { value =>
        println(s"$prefix $value")
        val stackTrace: StackTrace = value.getStackTrace()
        stackTrace.foreach { value =>
          value.foreach(println)
        }
      }
      same
  }

}
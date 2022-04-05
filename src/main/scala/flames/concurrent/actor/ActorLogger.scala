package flames.concurrent.actor

import flames.logging.*

import java.time.ZoneId
import java.time.format.DateTimeFormatter

trait ActorLogger(override val logLevel: LogLevel)
                 (using ActorRuntime) extends Logger {
  this: AnyActor[LogEvent] =>

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
object ActorLogger {
  
  def default(lvl: LogLevel, customThread: PinnedFiber.CustomThread)
             (using ActorRuntime): ActorLogger =
    new ActorLogger(lvl) with PinnedActor[LogEvent](customThread) {}
  
}
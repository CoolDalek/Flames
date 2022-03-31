package flames.concurrent

import flames.concurrent.ActorLogger.*
import flames.util.Logger
import flames.util.Logger.*

final class ActorLogger(val logLevel: LogLevel)
                       (using ActorRuntime) extends BlockingActor[LogEvent] with Logger {


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

  type StackTrace = Array[StackTraceElement] | Null

  extension [T](nullable: T | Null) {
    inline def foreach(action: T => Unit) =
      nullable match {
        case null => ()
        case value: T => action(value)
      }
  }

}
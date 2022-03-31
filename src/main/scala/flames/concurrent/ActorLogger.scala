package flames.concurrent

import flames.concurrent.ActorLogger.*
import flames.concurrent.ActorLogger.LogLevel.*
import flames.util.Logger
import sourcecode.{Enclosing, Line}
import scala.Ordering.Implicits._

final class ActorLogger(logLevel: LogLevel)(using ActorRuntime) extends BlockingActor[LogEvent] with Logger {

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

  inline def isEnabled(target: LogLevel): Boolean = target <= logLevel

  inline def log(lvl: LogLevel, msg: Message, exc: Failure)(using enc: Enclosing, line: Line): Unit =
    if(isEnabled(lvl)) {
      val prefix = s"[$lvl][${enc.value}][line:${line.value}]:"
      self.tell(
        LogEvent(prefix, msg, exc)
      )
    }

  override def isTraceEnabled: Boolean = isEnabled(Trace)

  override inline def trace(msg: String)(using Enclosing, Line): Unit =
    log(Trace, msg, null)

  override inline def trace(exc: Throwable)(using Enclosing, Line): Unit =
    log(Trace, null, exc)

  override inline def trace(exc: Throwable, msg: String)(using Enclosing, Line): Unit =
    log(Trace, msg, exc)

  override def isDebugEnabled: Boolean = isEnabled(Debug)

  override inline def debug(msg: String)(using Enclosing, Line): Unit =
    log(Debug, msg, null)

  override inline def debug(exc: Throwable)(using Enclosing, Line): Unit =
    log(Debug, null, exc)

  override inline def debug(exc: Throwable, msg: String)(using Enclosing, Line): Unit =
    log(Debug, msg, exc)

  override def isInfoEnabled: Boolean = isEnabled(Info)

  override inline def info(msg: String)(using Enclosing, Line): Unit =
    log(Info, msg, null)

  override inline def info(exc: Throwable)(using Enclosing, Line): Unit =
    log(Info, null, exc)

  override inline def info(exc: Throwable, msg: String)(using Enclosing, Line): Unit =
    log(Info, msg, exc)

  override def isWarnEnabled: Boolean = isEnabled(Warn)

  override inline def warn(msg: String)(using Enclosing, Line): Unit =
    log(Warn, msg, null)

  override inline def warn(exc: Throwable)(using Enclosing, Line): Unit =
    log(Warn, null, exc)

  override inline def warn(exc: Throwable, msg: String)(using Enclosing, Line): Unit =
    log(Warn, msg, exc)

  override def isErrorEnabled: Boolean = isEnabled(Error)

  override inline def error(msg: String)(using Enclosing, Line): Unit =
    log(Error, msg, null)

  override inline def error(exc: Throwable)(using Enclosing, Line): Unit =
    log(Error, null, exc)

  override inline def error(exc: Throwable, msg: String)(using Enclosing, Line): Unit =
    log(Error, msg, exc)

}
object ActorLogger {

  enum LogLevel {
    case Trace extends LogLevel
    case Debug extends LogLevel
    case Info extends LogLevel
    case Warn extends LogLevel
    case Error extends LogLevel
  }

  given Ordering[LogLevel] = (x: LogLevel, y: LogLevel) =>
    (x, y) match {
      case (Trace, Trace) => 0
      case (Trace, _) => 1
      case (_, Trace) => -1
      case (Debug, Debug) => 0
      case (Debug, _) => 1
      case (_, Debug) => -1
      case (Info, Info) => 0
      case (Info, _) => 1
      case (_, Info) => -1
      case (Warn, Warn) => 0
      case (Warn, _) => 1
      case (_, Warn) => -1
      case (Error, Error) => 0
    }

  type StackTrace = Array[StackTraceElement] | Null
  type Message = String | Null
  type Failure = Throwable | Null

  extension [T](nullable: T | Null) {
    inline def foreach(action: T => Unit) =
      nullable match {
        case null => ()
        case value: T => action(value)
      }
  }

  case class LogEvent(prefix: String, msg: Message, exc: Failure)

}
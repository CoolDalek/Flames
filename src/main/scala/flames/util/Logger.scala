package flames.util

import sourcecode.*
import Logger.*
import Logger.LogLevel.*
import Ordering.Implicits.*

trait Logger {

  def log(event: LogEvent): Unit

  def logLevel: LogLevel

  def isEnabled(target: LogLevel): Boolean = target <= logLevel

  inline def log(lvl: LogLevel, msg: Message, exc: Failure)(using enc: Enclosing, line: Line): Unit =
    if(isEnabled(lvl)) {
      val prefix = s"[$lvl][${enc.value}][line:${line.value}]:"
      log(LogEvent(prefix, msg, exc))
    }

  def isTraceEnabled: Boolean = isEnabled(Trace)

  inline def trace(msg: String)(using Enclosing, Line): Unit =
    log(Trace, msg, null)

  inline def trace(exc: Throwable)(using Enclosing, Line): Unit =
    log(Trace, null, exc)

  inline def trace(exc: Throwable, msg: String)(using Enclosing, Line): Unit =
    log(Trace, msg, exc)

  def isDebugEnabled: Boolean = isEnabled(Debug)

  inline def debug(msg: String)(using Enclosing, Line): Unit =
    log(Debug, msg, null)

  inline def debug(exc: Throwable)(using Enclosing, Line): Unit =
    log(Debug, null, exc)

  inline def debug(exc: Throwable, msg: String)(using Enclosing, Line): Unit =
    log(Debug, msg, exc)

  def isInfoEnabled: Boolean = isEnabled(Info)

  inline def info(msg: String)(using Enclosing, Line): Unit =
    log(Info, msg, null)

  inline def info(exc: Throwable)(using Enclosing, Line): Unit =
    log(Info, null, exc)

  inline def info(exc: Throwable, msg: String)(using Enclosing, Line): Unit =
    log(Info, msg, exc)

  def isWarnEnabled: Boolean = isEnabled(Warn)

  inline def warn(msg: String)(using Enclosing, Line): Unit =
    log(Warn, msg, null)

  inline def warn(exc: Throwable)(using Enclosing, Line): Unit =
    log(Warn, null, exc)

  inline def warn(exc: Throwable, msg: String)(using Enclosing, Line): Unit =
    log(Warn, msg, exc)

  def isErrorEnabled: Boolean = isEnabled(Error)

  inline def error(msg: String)(using Enclosing, Line): Unit =
    log(Error, msg, null)

  inline def error(exc: Throwable)(using Enclosing, Line): Unit =
    log(Error, null, exc)

  inline def error(exc: Throwable, msg: String)(using Enclosing, Line): Unit =
    log(Error, msg, exc)

}
object Logger {
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

  type Message = String | Null
  type Failure = Throwable | Null
  case class LogEvent(prefix: String, msg: Message, exc: Failure)

}
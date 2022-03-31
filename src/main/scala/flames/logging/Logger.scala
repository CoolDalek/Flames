package flames.logging

import sourcecode.*

import java.lang.Thread.UncaughtExceptionHandler
import java.time.Instant
import java.time.format.DateTimeFormatter

import scala.Ordering.Implicits.*

import LogLevel.*

trait Logger {

  def log(event: LogEvent): Unit

  def logLevel: LogLevel

  def timestampFormat: DateTimeFormatter

  def isEnabled(target: LogLevel): Boolean = target <= logLevel

  inline def log(lvl: LogLevel, msg: => Message, exc: Failure)(using enc: Enclosing, line: Line): Unit =
    if(isEnabled(lvl)) {
      val timestamp = timestampFormat.format(Instant.now())
      val prefix = s"[$lvl][$timestamp][${enc.value}][line:${line.value}]:"
      log(LogEvent(prefix, msg, exc))
    }

  def isTraceEnabled: Boolean = isEnabled(Trace)

  inline def trace(msg: => String)(using Enclosing, Line): Unit =
    log(Trace, msg, null)

  inline def trace(exc: Throwable)(using Enclosing, Line): Unit =
    log(Trace, null, exc)

  inline def trace(exc: Throwable, msg: => String)(using Enclosing, Line): Unit =
    log(Trace, msg, exc)

  def isDebugEnabled: Boolean = isEnabled(Debug)

  inline def debug(msg: => String)(using Enclosing, Line): Unit =
    log(Debug, msg, null)

  inline def debug(exc: Throwable)(using Enclosing, Line): Unit =
    log(Debug, null, exc)

  inline def debug(exc: Throwable, msg: => String)(using Enclosing, Line): Unit =
    log(Debug, msg, exc)

  def isInfoEnabled: Boolean = isEnabled(Info)

  inline def info(msg: => String)(using Enclosing, Line): Unit =
    log(Info, msg, null)

  inline def info(exc: Throwable)(using Enclosing, Line): Unit =
    log(Info, null, exc)

  inline def info(exc: Throwable, msg: => String)(using Enclosing, Line): Unit =
    log(Info, msg, exc)

  def isWarnEnabled: Boolean = isEnabled(Warn)

  inline def warn(msg: => String)(using Enclosing, Line): Unit =
    log(Warn, msg, null)

  inline def warn(exc: Throwable)(using Enclosing, Line): Unit =
    log(Warn, null, exc)

  inline def warn(exc: Throwable, msg: => String)(using Enclosing, Line): Unit =
    log(Warn, msg, exc)

  def isErrorEnabled: Boolean = isEnabled(Error)

  inline def error(msg: => String)(using Enclosing, Line): Unit =
    log(Error, msg, null)

  inline def error(exc: Throwable)(using Enclosing, Line): Unit =
    log(Error, null, exc)

  inline def error(exc: Throwable, msg: => String)(using Enclosing, Line): Unit =
    log(Error, msg, exc)

}
object Logger {

  def asUncaughtExceptionHandler(logger: Logger, logLevel: LogLevel = Error): UncaughtExceptionHandler =
    (t: Thread, e: Throwable) => logger.log(logLevel, s"Unexpected exception in thread ${t.getName}.", e)

}
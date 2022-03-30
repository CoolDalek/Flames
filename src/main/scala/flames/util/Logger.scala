package flames.util

import sourcecode.*

trait Logger {

  def isTraceEnabled: Boolean

  inline def trace(msg: String)(using Enclosing, Line): Unit

  inline def trace(exc: Throwable)(using Enclosing, Line): Unit

  inline def trace(exc: Throwable, msg: String)(using Enclosing, Line): Unit

  def isDebugEnabled: Boolean

  inline def debug(msg: String)(using Enclosing, Line): Unit

  inline def debug(exc: Throwable)(using Enclosing, Line): Unit

  inline def debug(exc: Throwable, msg: String)(using Enclosing, Line): Unit

  def isInfoEnabled: Boolean

  inline def info(msg: String)(using Enclosing, Line): Unit

  inline def info(exc: Throwable)(using Enclosing, Line): Unit

  inline def info(exc: Throwable, msg: String)(using Enclosing, Line): Unit

  def isWarnEnabled: Boolean

  inline def warn(msg: String)(using Enclosing, Line): Unit

  inline def warn(exc: Throwable)(using Enclosing, Line): Unit

  inline def warn(exc: Throwable, msg: String)(using Enclosing, Line): Unit

  def isErrorEnabled: Boolean

  inline def error(msg: String)(using Enclosing, Line): Unit
  
  inline def error(exc: Throwable)(using Enclosing, Line): Unit
  
  inline def error(exc: Throwable, msg: String)(using Enclosing, Line): Unit

}
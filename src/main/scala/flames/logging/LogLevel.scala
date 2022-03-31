package flames.logging

enum LogLevel {
  case Trace extends LogLevel
  case Debug extends LogLevel
  case Info extends LogLevel
  case Warn extends LogLevel
  case Error extends LogLevel
}
import LogLevel.*
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
package flames.logging

type Message = String | Null
type Failure = Throwable | Null
type StackTrace = Array[StackTraceElement] | Null
case class LogEvent(prefix: String, msg: Message, exc: Failure)
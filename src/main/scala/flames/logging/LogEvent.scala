package flames.logging

type Message = String | Null
type Failure = Throwable | Null
type StackTrace = Array[StackTraceElement] | Null
extension [T](nullable: T | Null) {
  inline def notNull[R](action: T => R) =
    nullable match {
      case null => ()
      case value: T => action(value)
    }
}
case class LogEvent(prefix: String, msg: Message, exc: Failure)
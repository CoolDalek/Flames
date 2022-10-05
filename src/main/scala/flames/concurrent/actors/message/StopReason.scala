package flames.concurrent.actors.message

enum StopReason {
  case Shutdown
  case Failure(exc: Throwable)
  case Unknown
}

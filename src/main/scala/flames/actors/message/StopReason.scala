package flames.actors.message

enum StopReason:
  case Failure(exc: Throwable)
  case Shutdown
  case Unknown

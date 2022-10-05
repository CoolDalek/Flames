package flames.concurrent.exception

import flames.concurrent.utils.*

abstract class OnlyReasonException(
                                    message: String,
                                    reason: Throwable | Null = null,
                                  ) extends RuntimeException(message) {

  def this(reason: Throwable) =
    this(reason.getMessage, reason)

  reason.notNull(initCause)

  final override def fillInStackTrace(): Throwable =
    setStackTrace {
      getCause match
        case null => Array.empty
        case cause => cause.getStackTrace
    }
    this
  end fillInStackTrace

}
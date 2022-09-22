package flames.actors.message

import scala.util.control.NoStackTrace

enum DeliveryFailure(
                      message: String,
                      reason: Throwable | Null = null,
                    ) extends RuntimeException(message, reason) {

  final override def fillInStackTrace(): Throwable =
    setStackTrace {
      getCause match
        case null  => Array.empty
        case cause => cause.getStackTrace
    }
    this
  end fillInStackTrace

  case TimedOut extends DeliveryFailure("Timeout")
  case Overflow extends DeliveryFailure("Overflow")
  case Connection(reason: Throwable) extends  DeliveryFailure("Connection", reason)
  case DeadLetter extends DeliveryFailure("DeadLetter")

}
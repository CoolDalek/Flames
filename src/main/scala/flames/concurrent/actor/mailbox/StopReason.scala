package flames.concurrent.actor.mailbox

import ReasonTag.*

sealed trait StopReason {
  def tag: ReasonTag
}
object StopReason {
  
  case object Shutdown extends StopReason {
    val tag: ReasonTag = ShutdownTag
  }
  
  case class Failure(exc: Throwable) extends StopReason {
    val tag: ReasonTag = FailureTag
  } 
  
}
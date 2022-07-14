package flames.concurrent.actor.mailbox

import ReasonTag.*

sealed trait StopReason {
  def tag: ReasonTag
}
object StopReason {
  
  object Shutdown extends StopReason {
    val tag: ReasonTag = ShutdownTag
  }
  
  class Failure(exc: Throwable) extends StopReason {
    val tag: ReasonTag = FailureTag
  } 
  
}
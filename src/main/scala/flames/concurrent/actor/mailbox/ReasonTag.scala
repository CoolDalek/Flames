package flames.concurrent.actor.mailbox

import ReasonTag.*

type ReasonTag = ShutdownTag | FailureTag
object ReasonTag {
  
  type ShutdownTag = 1
  val ShutdownTag: ShutdownTag = 1
  
  type FailureTag = 2
  val FailureTag: FailureTag = 2

}

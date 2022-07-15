package flames.concurrent.actor.mailbox

import ReasonTag.*

type ReasonTag = ShutdownTag | FailureTag
object ReasonTag {
  type ShutdownTag = 1
  inline val ShutdownTag: ShutdownTag = 1
  type FailureTag = 2
  inline val FailureTag: FailureTag = 2
}

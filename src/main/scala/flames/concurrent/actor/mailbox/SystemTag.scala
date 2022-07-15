package flames.concurrent.actor.mailbox

import SystemTag.*

type SystemTag = ChildStoppedTag
object SystemTag {
  type ChildStoppedTag = 1
  inline val ChildStoppedTag: ChildStoppedTag = 1
}

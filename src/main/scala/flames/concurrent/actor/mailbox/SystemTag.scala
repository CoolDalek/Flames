package flames.concurrent.actor.mailbox

import SystemTag.*

type SystemTag = ChildStoppedTag

object SystemTag {

  type ChildStoppedTag = 1
  val ChildStoppedTag: ChildStoppedTag = 1

}

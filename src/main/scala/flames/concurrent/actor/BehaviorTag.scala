package flames.concurrent.actor

import BehaviorTag.*

private[actor] type BehaviorTag = SameTag | ReceiveTag | StopTag
private[actor] object BehaviorTag {
  type SameTag = 1
  val SameTag: SameTag = 1
  type ReceiveTag = 2
  val ReceiveTag: ReceiveTag = 2
  type StopTag = 3
  val StopTag: StopTag = 3
}
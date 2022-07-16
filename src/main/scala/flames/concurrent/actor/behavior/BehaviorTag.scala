package flames.concurrent.actor.behavior

import BehaviorTag.*

type BehaviorTag = SameTag | ReceiveTag | StopTag
object BehaviorTag {
  type SameTag = 1
  inline val SameTag: SameTag = 1
  type ReceiveTag = 2
  inline val ReceiveTag: ReceiveTag = 2
  type StopTag = 3
  inline val StopTag: StopTag = 3
}

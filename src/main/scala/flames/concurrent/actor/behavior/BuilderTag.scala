package flames.concurrent.actor.behavior

import BuilderTag.*

type BuilderTag = ReceiveTag | HandleFailureTag | IgnoreTag
object BuilderTag {
  type ReceiveTag = 1
  inline val ReceiveTag: ReceiveTag = 1
  type HandleFailureTag = 2
  inline val HandleFailureTag: HandleFailureTag = 2
  type IgnoreTag = 3
  inline val IgnoreTag: IgnoreTag = 3
}
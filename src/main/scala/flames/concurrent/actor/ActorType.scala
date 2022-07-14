package flames.concurrent.actor

import ActorType.*

type ActorType = Blocking | Pinned | Async
object ActorType {
  type Blocking = 1
  val Blocking: Blocking = 1
  type Pinned = 2
  val Pinned: Pinned = 2
  type Async = 3
  val Async: Async = 3
}
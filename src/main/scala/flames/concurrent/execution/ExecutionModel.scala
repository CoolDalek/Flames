package flames.concurrent.execution

import ExecutionModel.*

type ExecutionModel = Blocking | Pinned | Async
object ExecutionModel {
  type Blocking = 1
  inline val Blocking: Blocking = 1
  type Pinned = 2
  inline val Pinned: Pinned = 2
  type Async = 3
  inline val Async: Async = 3
}
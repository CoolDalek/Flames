package flames.actors.path

import flames.actors.*

sealed trait SelectionResult[-T]
object SelectionResult {
  case object NotFound extends SelectionResult[Any]
  case class FoundOne[T](ref: ActorRef[T]) extends SelectionResult[T]
  case class FoundMany[T](set: Set[ActorRef[T]]) extends SelectionResult[T]

  def make[T](set: Set[ActorRef[T]]): SelectionResult[T] =
    if (set.isEmpty) NotFound
    else if (set.size == 1) FoundOne(set.head)
    else FoundMany(set)
  end make

}

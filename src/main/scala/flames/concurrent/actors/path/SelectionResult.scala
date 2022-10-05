package flames.concurrent.actors.path

import flames.concurrent.actors.*
import flames.concurrent.actors.ActorRef

enum SelectionResult[-T] {
  case NotFound extends SelectionResult[Any]
  case FoundOne[T](ref: ActorRef[T]) extends SelectionResult[T]
  case FoundMany[T](set: Set[ActorRef[T]]) extends SelectionResult[T]
}
object SelectionResults {
  import SelectionResult.*

  def make[T](set: Set[ActorRef[T]]): SelectionResult[T] =
    if(set.isEmpty) NotFound
    else if(set.size == 1) FoundOne(set.head)
    else FoundMany(set)
  end make

}

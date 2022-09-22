package flames.actors.message

import java.util.Queue as JQueue
import scala.collection.mutable.Queue as SQueue

trait Queue[C[_]] {

  extension [T](self: C[T]) {

    def poll(): T | Null

    def push(elem: T): Boolean
    
    def isEmpty: Boolean

  }

}
object Queue {

  given [C[X] <: JQueue[X]]: Queue[C] with
    extension [T](self: C[T])
      def poll(): T | Null = self.poll()
      def push(elem: T): Boolean = self.offer(elem)
      def isEmpty: Boolean = self.isEmpty()
  end given

  given Queue[SQueue] with
    extension [T](self: SQueue[T])
      def poll(): T | Null = self.dequeue()
      def push(elem: T): Boolean = {self.enqueue(elem); true}
      def isEmpty: Boolean = self.isEmpty
  end given
  
}
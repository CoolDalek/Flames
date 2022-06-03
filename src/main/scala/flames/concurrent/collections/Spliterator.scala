package flames.concurrent.collections

import scala.annotation.tailrec

trait Spliterator[+T] { self =>
  
  def head: UnsafeIterator[T] | Null
  
  def tail: IArray[UnsafeIterator[T]]
  
  def size: Int

  def asIterator(): Iterator[T] =
    new Iterator[T] {
      private var current = self.head
      private var cursor = -1

      override def hasNext: Boolean =
        current != null && {
          current.hasNext || {
            val tail = self.tail
            var i = cursor
            var found = false
            while (i < tail.length && !found) {
              found = tail(i).hasNext
              i += 1
            }
            found
          }
        }

      @tailrec
      final override def next(): T =
        if(current != null) {
          if(current.hasNext) {
            current.next()
          } else {
            cursor += 1
            if(cursor < self.tail.length) {
              current = self.tail(cursor)
              next()
            } else throw new NoSuchElementException()
          }
        } else throw new NoSuchElementException()

    }

}
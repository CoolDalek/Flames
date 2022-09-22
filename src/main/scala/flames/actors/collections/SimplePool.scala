package flames.actors.collections

import flames.actors.utils.Nulls.*
import Pool.Reuse
import SimplePool.Storage

import java.util
import scala.collection.mutable

class SimplePool[T: Reuse, C[_]: Storage](
                                           min: Int,
                                           max: Int,
                                           make: () => T,
                                           storage: C[T],
                                         ) extends Pool[T]:

  locally {
    var i = 0
    while (i < min)
      i += 1
      storage.add(make())
  }

  override def alloc(): T =
    storage.poll()
      .ifNull(make())

  override def dealloc(used: T): Unit =
    if (storage.size() < max)
      used.clear()
      storage.add(used)
  end dealloc

object SimplePool:

  trait Storage[C[_]] {

    extension[T] (self: C[T]) {

      def add(value: T): Unit

      def poll(): T | Null

      def size(): Int

    }

  }

  given JQueue[C[x] <: util.Queue[x]]: Storage[C] with
    extension [T](self: C[T])

      inline def add(value: T): Unit = self.offer(value)

      inline def poll(): T | Null = self.poll()

      inline def size(): Int = self.size()

  end JQueue

  given Storage[mutable.Queue] with
    extension[T] (self: mutable.Queue[T])

      inline def add(value: T): Unit = self.addOne(value)

      inline def poll(): T | Null = self.dequeue()

      inline def size(): Int = self.length

  end given
  
  given Storage[mutable.ArrayBuffer] with
    extension[T] (self: mutable.ArrayBuffer[T])
  
      inline def add(value: T): Unit = self.addOne(value)
  
      inline def poll(): T | Null =
        if(self.nonEmpty)
          val last = self.length - 1
          val value = self(last)
          self.remove(last)
          value
        else null
      end poll
  
      inline def size(): Int = self.length
  end given

  given Storage[mutable.ListBuffer] with
    extension[T] (self: mutable.ListBuffer[T])

      inline def add(value: T): Unit = self.addOne(value)

      inline def poll(): T | Null =
        if (self.nonEmpty)
          val value = self.head
          self.remove(0)
          value
        else null
      end poll

      inline def size(): Int = self.length
  end given

end SimplePool
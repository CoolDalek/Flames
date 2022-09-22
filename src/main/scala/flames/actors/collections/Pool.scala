package flames.actors.collections

import flames.actors.utils.*

trait Pool[T]:

  def alloc(): T

  def dealloc(used: T): Unit

object Pool:

  trait Reuse[T] {

    extension (self: T)
      def clear(): Unit

  }

  @FunctionalInterface
  trait Factory {

    def apply[T: Reuse](min: Int, max: Int, make: () => T): Pool[T]

  }

  import scala.collection.mutable

  inline def simple[T: Reuse](min: Int, max: Int, make: () => T): Pool[T] =
    new SimplePool[T, mutable.ArrayBuffer](
      min = min,
      max = max,
      make = make,
      storage = {
        val buffer = mutable.ArrayBuffer.empty[T]
        buffer.sizeHint(min)
        buffer
      },
    )

  inline def shared[T: Reuse](between: Int, min: Int, max: Int, make: () => T): Pool[T] =
    new SharedPool[T](
      between = between,
      min = min,
      max = max,
      makeElement = make,
      makePool = new Factory {
        override def apply[T: Reuse](min: Int, max: Int, make: () => T): Pool[T] = simple(min, max, make)
      },
    )

end Pool

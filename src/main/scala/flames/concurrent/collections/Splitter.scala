package flames.concurrent.collections

import flames.util.SummonerK

trait Splitter[C[_]] {

  def split[T](coll: C[T]): Spliterator[T]

}
object Splitter extends SummonerK[Splitter] {

  given Splitter[IArray] = new Splitter[IArray] {
    private val partSize = 10

    override def split[T](coll: IArray[T]): Spliterator[T] =
      new Spliterator[T] { self =>
        private var current = 0
        private var splitted = false
        override val knownSize: Int = coll.length / partSize
        private var head = null.asInstanceOf[UnsafeIterator[T]]
        private val tail = new Array[UnsafeIterator[T]](
          math.max(knownSize - 1, 0)
        )
        override def forkCache: IArray[UnsafeIterator[T]] =
          tail.asInstanceOf[IArray[UnsafeIterator[T]]]

        override def forceSplit(): Unit =
          if(!splitted) {
            val cursor = current
            current = 0
            if(null == head && hasNext) head = next()
            var position = 0
            while(hasNext) {
              position = current - 1 
              if (null == tail(position)) tail(position) = next()
            }
            current = cursor
            splitted = true
          }

        override def reset(): Unit = {
          current = 0
          if(null != head) head.reset()
          tail.foreach { value =>
            if(null != value) value.reset()
          }
        }

        override def hasNext: Boolean = current < knownSize

        override def next(): UnsafeIterator[T] = {
          val cached = if(current == 0) head else tail(current - 1)
          if (null == cached) {
            val next = new UnsafeIterator[T] {
              private val cache = {
                val start = self.current * partSize
                val end = math.min(coll.length, start + partSize)
                val length = end - start
                val array = new Array[Any](length)
                System.arraycopy(
                  coll.asInstanceOf[Array[T]], start,
                  array, 0,
                  length,
                )
                array
              }
              private var current = 0

              override def hasNext: Boolean = current < cache.length

              override def next(): T = cache(current).asInstanceOf[T]

              override def reset(): Unit = current = 0

              override def map[R](f: T => R): UnsafeIterator[R] = {
                cache.mapInPlace(f.asInstanceOf[Any => Any])
                this.asInstanceOf[UnsafeIterator[R]]
              }

            }
            if(current == 0) head = next
            else tail(current - 1) = next
            if(!hasNext) splitted = true
            current += 1
            next
          } else cached
        }
      }
    
  }

}
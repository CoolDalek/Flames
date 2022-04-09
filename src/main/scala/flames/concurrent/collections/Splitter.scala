package flames.concurrent.collections

import flames.util.SummonerK

trait Splitter[C[_]] {

  def split[T](coll: C[T]): Iterator[Iterator[T]]

  def iterator[T](coll: C[T]): Iterator[T]

}
object Splitter extends SummonerK[Splitter] {

  given Splitter[IArray] = new Splitter[IArray] {

    override def split[T](coll: IArray[T]): Iterator[Iterator[T]] =
      new Iterator[Iterator[T]] { self =>
        private var current = 0

        private val parts: Int = coll.length / 100

        override def knownSize: Int = parts

        override def hasNext: Boolean = current < parts

        override def next(): Iterator[T] = {
          val next = new Iterator[T] {
            private val part = self.current
            private var current = part * 100
            private val end = math.min(coll.length, current + 100)

            override def hasNext: Boolean = current < end

            override def next(): T = {
              val next = coll(current)
              current += 1
              next
            }
          }
          current += 1
          next
        }
      }

    override def iterator[T](coll: IArray[T]): Iterator[T] =
      coll.iterator

  }

}
package flames.concurrent.collections

import flames.util.SummonerK

trait Splitter[C[_]] {

  def split[T](coll: C[T]): Iterator[Iterator[T]]

  def iterator[T](coll: C[T]): Iterator[T]

}
object Splitter extends SummonerK[Splitter] {

  given Splitter[IArray] = new Splitter[IArray] {

    override def split[T](coll: IArray[T]): Iterator[Iterator[T]] =
      new Iterator[Iterator[T]] {
        private var currentPart = 0
        private val parts: Int = coll.length / 100

        override def hasNext: Boolean = currentPart < parts

        override def next(): Iterator[T] = {
          val next = new Iterator[T] {
            private val part = currentPart
            private var currentElement = part * 100
            private val end = math.min(coll.length, currentElement + 100)

            override def hasNext: Boolean = currentElement < end

            override def next(): T = {
              val next = coll(currentElement)
              currentElement += 1
              next
            }
          }
          currentPart += 1
          next
        }

      }

    override def iterator[T](coll: IArray[T]): Iterator[T] =
      coll.iterator

  }

}
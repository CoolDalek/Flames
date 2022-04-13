package flames.concurrent.collections

trait Spliterator[+T] { self =>

  def hasNext: Boolean

  def next(): UnsafeIterator[T]

  def reset(): Unit
  
  def forkCache: IArray[UnsafeIterator[T]]
  
  def forceSplit(): Unit
  
  def knownSize: Int

  def collect(): Iterator[T] = {
    reset()
    new Iterator[T] {
      private var currentPart = null.asInstanceOf[UnsafeIterator[T]]

      override def hasNext: Boolean =
        self.hasNext || (null != currentPart && currentPart.hasNext)

      override def next(): T = {
        if (null == currentPart) {
          currentPart = self.next()
        }
        currentPart.next()
      }

    }
  }

}
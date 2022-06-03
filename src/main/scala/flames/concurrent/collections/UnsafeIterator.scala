package flames.concurrent.collections

trait UnsafeIterator[+T] {

  def hasNext: Boolean

  def next(): T

  def reset(): Unit

  def map[R](f: T => R): UnsafeIterator[R]
  
  def flatMap[R](f: T => Spliterator[R]): UnsafeIterator[R]

}
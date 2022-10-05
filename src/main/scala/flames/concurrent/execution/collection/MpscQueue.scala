package flames.concurrent.execution.collection

/**
 * Multi-Producer-Shifting-Consumer
 * Queue will be working correctly as long as there is only one consumer at the time
 * and consumer side is properly acquired and released.
 * */
class MpscQueue[T] {

  def push(element: T): Boolean = ???

  def poll()(using Consuming): T | Null = ???

  def consume(f: Consumer): Unit = ???

  trait Consuming
  type Consumer = Consuming ?=> Unit

}

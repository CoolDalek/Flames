package flames.concurrent.execution.collection

import flames.concurrent.utils.Maybe

trait ProducerQueue[+T]:

  def poll: Maybe[T]

  def drain(consumer: T => Unit): Unit
  
  def isEmpty: Boolean
  
  def nonEmpty: Boolean

trait ConsumerQueue[-T]:

  def push(element: T): Boolean

  def fill(producer: => Maybe[T]): Boolean
  
  def isFull: Boolean
  
  def nonFull: Boolean

trait MessageQueue[T] extends ProducerQueue[T], ConsumerQueue[T]

package flames.actors.message

import flames.actors.utils.*
import SystemMessage.InternalMessage

import java.util.Queue as JQueue
import java.util.concurrent.ConcurrentLinkedQueue
import scala.language.postfixOps

trait Mailbox[T] {
  
  def isEmpty: Boolean

  def pushTimer(msg: T): Boolean

  def pushUser(msg: T): Boolean

  def pushInternal(msg: InternalMessage): Boolean

  def pollProtocol(): T | Null

  def pollInternal(): InternalMessage | Null
  
  def drainProtocol(consumer: T => Unit): Unit
  
  def drainInternal(consumer: InternalMessage => Unit): Unit

}
object Mailbox {

  def fromQueue[T, User[_]: Queue, Timer[_]: Queue, Internal[_]: Queue](
                                                                         user: User[T],
                                                                         timer: Timer[T],
                                                                         internal: Internal[InternalMessage],
                                                                       ): Mailbox[T] = new:

    override def isEmpty: Boolean = user.isEmpty && timer.isEmpty && internal.isEmpty

    override def pushTimer(msg: T): Boolean =
      timer.push(msg)

    override def pushUser(msg: T): Boolean =
      user.push(msg)

    override def pushInternal(msg: InternalMessage): Boolean =
      internal.push(msg)

    override def pollProtocol(): T | Null =
      timer.poll().ifNull(user.poll())

    override def pollInternal(): InternalMessage | Null =
      internal.poll()
    
    private def drain[R, C[_]: Queue](queue: C[R], consumer: R => Unit): Unit =
      while(!queue.isEmpty) consumer(queue.poll().asInstanceOf[R])
    
    override def drainProtocol(consumer: T => Unit): Unit =
      drain(timer, consumer); drain(user, consumer)
    
    override def drainInternal(consumer: InternalMessage => Unit): Unit =
      drain(internal, consumer)

  end fromQueue

  def concurrentLinkedQueue[T]: Mailbox[T] =
    fromQueue(
      new ConcurrentLinkedQueue[T](),
      new ConcurrentLinkedQueue[T](),
      new ConcurrentLinkedQueue[InternalMessage](),
    )

}
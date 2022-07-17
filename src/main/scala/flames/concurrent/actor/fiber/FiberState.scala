package flames.concurrent.actor.fiber

import flames.concurrent.execution.*
import flames.concurrent.actor.mailbox.{Mailbox, SystemMessage}
import flames.concurrent.actor.{ActorParent, ActorRef, ActorPath}
import flames.util.Nullable.*
import org.jctools.queues.MpscLinkedQueue
import org.jctools.queues.atomic.{MpscUnboundedAtomicArrayQueue, SpscUnboundedAtomicArrayQueue}

import scala.collection.mutable

trait FiberState[T](
                     val config: FiberConfig,
                     val parent: ActorParent,
                     val path: ActorPath[T],
                   ) extends HasChilds {

  def systemMail: Mailbox[SystemMessage]

  def userMail: Mailbox[T]

  def systemPut(msg: SystemMessage): Unit

  def timerPut(msg: T): Unit

  def put(msg: T): Unit

  def hasMessage: Boolean = !(userMail.isEmpty && systemMail.isEmpty)

  def procState: AtomicProcessState

  var yieldCount: Int = 0
  var deadline: Long = 0
  var loop: Boolean = false

  def prepare(): Unit = {
    yieldCount = config.autoYieldCount
    deadline = config.autoYieldTime match {
      case Some(value) =>
        System.nanoTime() + value.toNanos
      case None =>
        Long.MaxValue
    }
    loop = true
  }

}
object FiberState {
  
  private class Default[T](
                            timerThreadsCount: Int,
                            config: FiberConfig,
                            parent: ActorParent,
                            token: ActorPath[T],
                          ) extends FiberState[T](config, parent, token) with HasChilds.Sync {
    import config.*

    //Single consumer because only 1 thread can read this at a time,
    // and every thread change synchronized on execution context.
    // At least, I hope this is fine...
    private val userQueue = new MpscUnboundedAtomicArrayQueue[T](userChunkSize)
    
    private val timerQueue = if(timerThreadsCount > 1) {
      new MpscUnboundedAtomicArrayQueue[T](timerChunkSize)
    } else {
      new SpscUnboundedAtomicArrayQueue[T](timerChunkSize)
    }
    // There is no common system event bus, so messages can be passed from different threads simultaneously
    private val systemQueue = new MpscUnboundedAtomicArrayQueue[SystemMessage](systemChunkSize)

    override val systemMail: Mailbox[SystemMessage] = new Mailbox[SystemMessage] {

      override def isEmpty: Boolean = systemQueue.isEmpty

      override def poll(): SystemMessage | Null = systemQueue.poll()

    }

    override val userMail: Mailbox[T] = new Mailbox[T] {

      override def isEmpty: Boolean = userQueue.isEmpty && timerQueue.isEmpty

      override def poll(): T | Null =
        timerQueue.poll()
          .orElse {
            userQueue.poll()
          }

    }

    override def systemPut(msg: SystemMessage): Unit =
      systemQueue.add(msg)

    override def timerPut(msg: T): Unit =
      timerQueue.add(msg)

    override def put(msg: T): Unit =
      userQueue.add(msg)

    override val procState: AtomicProcessState = AtomicProcessState(ProcessState.Idle)
    
  }

  def default[T](
                  timerThreadsCount: Int,
                  config: FiberConfig,
                  parent: ActorParent,
                  token: ActorPath[T],
                ): FiberState[T] =
    new Default(timerThreadsCount, config, parent, token)

}
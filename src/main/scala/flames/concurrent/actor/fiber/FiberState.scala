package flames.concurrent.actor.fiber

import flames.concurrent.{AtomicProcessState, ProcessState}
import flames.concurrent.actor.mailbox.{Mailbox, SystemMessage}
import flames.concurrent.actor.{ActorParent, ActorRef, ActorToken}
import flames.util.Nullable.*
import org.jctools.queues.MpscLinkedQueue
import org.jctools.queues.atomic.{MpscUnboundedAtomicArrayQueue, SpscUnboundedAtomicArrayQueue}

import scala.collection.mutable

trait FiberState[T](
                     val config: FiberConfig,
                     val parent: ActorParent,
                     val token: ActorToken,
                   ) {

  def systemMail: Mailbox[SystemMessage]

  def userMail: Mailbox[T]

  def systemPut(msg: SystemMessage): Unit

  def timerPut(msg: T): Unit

  def put(msg: T): Unit

  def hasMessage: Boolean = !(userMail.isEmpty && systemMail.isEmpty)

  def addChild(token: ActorToken, actor: ActorRef[Nothing]): Unit

  def removeChild(token: ActorToken): Option[ActorRef[Nothing]]

  def getChilds: Set[ActorRef[Nothing]]

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

  def default[T](_config: FiberConfig, _parent: ActorRef[Nothing] | Null, _token: ActorToken): FiberState[T] =
    new FiberState[T](_config, _parent, _token) {
      import config.*

      //Single consumer because only 1 thread can read this at a time,
      // and every thread change synchronized on execution context.
      // At least, I hope this is fine...
      private val userQueue = new MpscUnboundedAtomicArrayQueue[T](userChunkSize)
      // There is only 1 timer thread by default, so single producer is fine
      private val timerQueue = new SpscUnboundedAtomicArrayQueue[T](timerChunkSize)
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

      private val childs = mutable.Map.empty[ActorToken, ActorRef[Nothing]]

      override def addChild(token: ActorToken, actor: ActorRef[Nothing]): Unit =
        childs.update(token, actor)

      override def removeChild(token: ActorToken): Option[ActorRef[Nothing]] =
        childs.remove(token)

      override def getChilds: Set[ActorRef[Nothing]] =
        childs.values to Set

      override val procState: AtomicProcessState = AtomicProcessState(ProcessState.Idle)

    }

}
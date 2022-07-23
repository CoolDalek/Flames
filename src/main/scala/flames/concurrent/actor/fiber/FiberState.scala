package flames.concurrent.actor.fiber

import flames.concurrent.execution.*
import flames.concurrent.execution.ExecutionModel.*
import flames.concurrent.actor.mailbox.{Mailbox, SystemMessage}
import flames.concurrent.actor.*
import flames.util.Nullable.*
import org.jctools.queues.*
import org.jctools.queues.atomic.*

import scala.collection.mutable
import scala.concurrent.duration.FiniteDuration

trait FiberState[T](
                     val autoYieldCount: Int,
                     val autoYieldTime: Option[FiniteDuration],
                     val parent: ActorParent,
                     val path: ActorPath[T],
                   ) extends HasChilds with AcquireRelease {

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
    yieldCount = autoYieldCount
    deadline = autoYieldTime match {
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
                            autoYieldCount: Int,
                            autoYieldTime: Option[FiniteDuration],
                            userQueue: Mailbox.Consuming[T],
                            timerQueue: Mailbox.Consuming[T],
                            val systemMail: Mailbox.Consuming[SystemMessage],
                            val userMail: Mailbox[T],
                            parent: ActorParent,
                            path: ActorPath[T],
                            ar: AcquireRelease,
                          ) extends FiberState[T](autoYieldCount, autoYieldTime, parent, path) with HasChilds.Sync {
    export ar.*

    override def systemPut(msg: SystemMessage): Unit =
      systemMail.put(msg)

    override def timerPut(msg: T): Unit =
      timerQueue.put(msg)

    override def put(msg: T): Unit =
      userQueue.put(msg)

    override val procState: AtomicProcessState = AtomicProcessState(ProcessState.Idle)
    
  }

  def apply[T](
                post: Mailbox.PostOffice[T],
                config: ActorsConfig,
                parent: ActorParent,
                path: ActorPath[T],
                ar: AcquireRelease,
              ): FiberState[T] =
    new Default(
      autoYieldCount = config.autoYieldCount,
      autoYieldTime = config.autoYieldTime,
      userQueue = post.userQueue,
      timerQueue = post.timerQueue,
      systemMail = post.systemMail,
      userMail = post.userMail,
      parent = parent,
      path = path,
      ar = ar,
    )

}
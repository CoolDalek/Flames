package flames.concurrent.actor.mailbox

import flames.concurrent.actor.*
import flames.concurrent.execution.*
import flames.util.Nullable.*
import org.jctools.queues.MessagePassingQueue
import org.jctools.queues.atomic.*

trait Mailbox[+T] {

  def isEmpty: Boolean

  def poll(): T | Null

}
object Mailbox {

  trait Biased[+T] extends Mailbox[T] with AcquireRelease

  trait Consuming[T] extends Mailbox[T] {

    def put(msg: T): Unit

  }

  trait Protocol[+T](timer: Consuming[T], user: Consuming[T]) extends Biased[T] {

    override def isEmpty: Boolean = user.isEmpty && timer.isEmpty

    override def poll(): T | Null =
      timer.poll().orElse(user.poll())

  }

  def protocolShifting[T](timer: Consuming[T], user: Consuming[T]): Biased[T] =
    new Protocol[T](timer, user) with AcquireRelease.Volatile {}

  def protocolPinned[T](timer: Consuming[T], user: Consuming[T]): Biased[T] =
    new Protocol[T](timer, user) with AcquireRelease.Noop {}

  def jctools[T](queue: MessagePassingQueue[T]): Consuming[T] = new Consuming[T] {
    
    override def put(msg: T): Unit = queue.offer(msg)

    override def isEmpty: Boolean = queue.isEmpty

    override def poll(): T | Null = queue.poll()
    
  }
  
  def mpsc[T](chunk: Int, max: Int): Consuming[T] = jctools(
    new MpscChunkedAtomicArrayQueue[T](chunk, max)
  )
  
  def spsc[T](chunk: Int, max: Int): Consuming[T] = jctools(
    new SpscChunkedAtomicArrayQueue[T](chunk, max)
  )

  class Post[T](
                 val userQueue: Consuming[T],
                 val timerQueue: Consuming[T],
                 val systemMail: Consuming[SystemMessage],
                 val userMail: Biased[T]
               )

  trait Factory {

    def apply[T](model: ExecutionModel, config: ActorsConfig): Post[T]

  }

  val defaultFactory: Factory = new Factory {
    
    override def apply[T](model: ExecutionModel, config: ActorsConfig): Post[T] = {
      val user = mpsc[T](
        config.queueInitSize,
        config.queueMaxSize,
      )
      val timer = if(config.timerThreads > 1) mpsc[T](
        config.queueInitSize,
        config.queueMaxSize,
      ) else spsc[T](
        config.queueInitSize,
        config.queueMaxSize,
      )
      val system = mpsc[SystemMessage](
        config.queueInitSize,
        config.queueMaxSize,
      )
      val mail = model match {
        case ExecutionModel.Pinned =>
          protocolPinned[T](timer, user)
        case _ =>
          protocolShifting[T](timer, user)
      }
      new Post[T](
        userQueue = user,
        timerQueue = timer,
        systemMail = system,
        userMail = mail,
      )
    }
    
  }

}
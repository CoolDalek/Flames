package flames.concurrent.actor

import flames.concurrent.{AtomicProcessState, ProcessState}
import flames.concurrent.ProcessState.*

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicReference
import scala.annotation.{switch, tailrec}
import scala.collection.mutable
import scala.util.control.NonFatal
import BehaviorTag.*

trait ActorFiber[T](
                     protected final val runtime: ActorRuntime,
                     private var behavior: Behavior[T],
                   ) {

  import ProcessResult.*

  private type ProcessResult = EmptyQueue | Continue | Break
  private object ProcessResult {
    type EmptyQueue = 1
    val EmptyQueue: EmptyQueue = 1
    type Continue = 2
    val Continue: Continue = 2
    type Break = 3
    val Break: Break = 3
  }

  protected val timerQueue: ConcurrentLinkedQueue[T] = ConcurrentLinkedQueue[T]()
  protected val userQueue: ConcurrentLinkedQueue[T] = ConcurrentLinkedQueue[T]()
  protected val state: AtomicProcessState = AtomicProcessState(ProcessState.Idle)
  private val childs = mutable.Set.empty[ActorRef[Nothing]]

  private[concurrent] final def addChild(actor: ActorRef[Nothing]): Unit =
    childs + actor

  private[concurrent] final def getChilds: Set[ActorRef[Nothing]] =
    childs to Set

  private def stopCleanup(): Unit =
    childs.foreach(_.stop())

  @tailrec
  private[concurrent] final def stop(): Unit =
    (state.get(): @switch) match {
      case Idle =>
        if(state.compareAndSet(Idle, Stop)) {
          stopCleanup()
        } else stop()
      case Running =>
        state.set(Stop)
      case Stop => ()
    }

  private[concurrent] final def timerTell(message: T): Unit = tell(timerQueue, message)

  private[concurrent] final def userTell(message: T): Unit = tell(userQueue, message)

  private def tell(queue: ConcurrentLinkedQueue[T], message: T): Unit =
    (state.get(): @switch) match {
      case Stop =>
        runtime.reportFailure(Undelivered(message))
      case Running =>
        queue.add(message)
      case Idle =>
        queue.add(message)
        continue()
    }

  private var yieldCount: Int = _
  private var deadline: Long = _
  protected var loop: Boolean = _

  protected final def prepare(): Unit = {
    yieldCount = runtime.autoYieldCount
    deadline = runtime.autoYieldTime match {
      case Some(value) =>
        System.nanoTime() + value.toNanos
      case None =>
        Long.MaxValue
    }
    loop = true
  }

  protected final def executionLoop(): Unit = {
    prepare()
    while (loop) {
      if (yieldCount > 0 && System.nanoTime() < deadline) {
        process(timerQueue) {
          process(userQueue) {
            trySleep()
          }
        }
      } else {
        yieldExecution()
      }
    }
  }

  protected final def hasMessage: Boolean = !userQueue.isEmpty || !timerQueue.isEmpty

  protected def trySleep(): Unit

  protected def yieldExecution(): Unit

  protected def continue(): Unit

  protected def run(): Unit

  private def process(queue: ConcurrentLinkedQueue[T])(onEmpty: => Unit): Unit = {
    import ProcessResult.*
    (processMessage(queue): @switch) match {
      case EmptyQueue =>
        onEmpty
      case Continue =>
        yieldCount -= 1
      case Break =>
        stopCleanup()
        loop = false
    }
  }

  private def processMessage(queue: ConcurrentLinkedQueue[T]): ProcessResult = {
    if (state.get() != Stop) {
      if (queue.isEmpty) EmptyQueue
      else {
        (behavior.tag: @switch) match {
          case ReceiveTag =>
            try {
              val receive = behavior.asInstanceOf[Behavior.Receive[T]]
              val next = receive.act(queue.poll())
              (next.tag: @switch) match {
                case SameTag =>
                  behavior = receive
                case _ =>
                  behavior = next
              }
            } catch {
              case NonFatal(exc) =>
                runtime.reportFailure(exc)
                behavior = Behavior.Stop
            }
          case _ => ()
        }
        (behavior.tag: @switch) match {
          case StopTag =>
            state.set(Stop)
            Break
          case _ =>
            Continue
        }
      }
    } else Break
  }

}
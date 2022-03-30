package flames.concurrent

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicReference
import scala.annotation.tailrec
import scala.collection.mutable
import scala.util.control.NonFatal

final class ActorFiber[T] private[concurrent](
                                               private val runtime: ActorRuntime, 
                                               private var behavior: Behavior[T],
                                               private val path: ActorPath[T],
                                             ) {

  enum FiberState {
    case Stop extends FiberState
    case Running extends FiberState
    case Idle extends FiberState
  }

  enum ProcessResult {
    case EmptyQueue extends ProcessResult
    case Continue extends ProcessResult
    case Break extends ProcessResult
  }

  private val timerQueue = ConcurrentLinkedQueue[T]()
  private val userQueue = ConcurrentLinkedQueue[T]()
  private val state = AtomicReference[FiberState](FiberState.Idle)
  private val childs = mutable.Set.empty[ActorRef[Nothing]]

  private[concurrent] def addChild(actor: ActorRef[Nothing]): Unit =
    childs + actor

  private[concurrent] def getChilds: Set[ActorRef[Nothing]] =
    childs to Set

  private def stopCleanup(): Unit = {
    childs.foreach(_.stop())
    runtime.removeRef(path)
  }

  @tailrec
  private[concurrent] def stop(): Unit = {
    import FiberState.*
    state.get() match {
      case Idle =>
        if(state.compareAndSet(Idle, Stop)) {
          stopCleanup()
        } else stop()
      case Running =>
        state.set(Stop)
      case Stop => ()
    }
  }

  private[concurrent] def timerTell(message: T): Unit = tell(timerQueue, message)

  private[concurrent] def userTell(message: T): Unit = tell(userQueue, message)

  private def tell(queue: ConcurrentLinkedQueue[T], message: T): Unit = {
    import FiberState.*
    state.get() match {
      case Stop =>
        runtime.reportFailure(Undelivered(message))
      case Running =>
        queue.add(message)
      case Idle =>
        queue.add(message)
        start()
    }
  }

  private def deadlineTime(): Long =
    runtime.autoYieldTime match {
      case Some(value) =>
        System.nanoTime() + value.toNanos
      case None =>
        Long.MaxValue
    }

  private def start(): Unit = {
    import FiberState.*
    if (state.compareAndSet(Idle, Running)) run()
  }

  private def run(): Unit =
    runtime.execute {
      loop(
        runtime.autoYieldCount,
        deadlineTime(),
      )
    }

  private def loop(maxMessages: Int, deadline: Long): Unit = {
    import ProcessResult.*
    var loop = true
    var yieldCount = maxMessages

    while(loop) {

      def process(queue: ConcurrentLinkedQueue[T])(onEmpty: => Unit): Unit =
        processMessage(queue) match {
          case EmptyQueue =>
            onEmpty
          case Continue =>
            yieldCount -= 1
          case Break =>
            stopCleanup()
            loop = false
        }

      if (yieldCount > 0 && System.nanoTime() > deadline) {
        process(timerQueue) {
          process(userQueue) {
            state.set(FiberState.Idle)
            if(!userQueue.isEmpty || !timerQueue.isEmpty) start()
            loop = false
          }
        }
      } else {
        run()
        loop = false
      }

    }
  }

  private def processMessage(queue: ConcurrentLinkedQueue[T]): ProcessResult = {
    if (state.get() != FiberState.Stop) {
      if (queue.isEmpty) ProcessResult.EmptyQueue
      else {
        behavior match {
          case receive: Behavior.Receive[T] =>
            try {
              receive.act(queue.poll()) match {
                case Behavior.Same =>
                  behavior = receive
                case next =>
                  behavior = next
              }
            } catch {
              case NonFatal(exc) =>
                runtime.reportFailure(exc)
                behavior = Behavior.Stop
            }
          case _ => ()
        }
        behavior match {
          case Behavior.Stop =>
            ProcessResult.Break
          case _ =>
            ProcessResult.Continue
        }
      }
    } else ProcessResult.Break
  }

}
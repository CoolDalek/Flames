package flames.concurrent

import java.util.concurrent.{ConcurrentLinkedQueue, RejectedExecutionException}
import java.util.concurrent.atomic.AtomicReference
import scala.annotation.tailrec
import scala.concurrent.duration.FiniteDuration
import scala.util.control.{ControlThrowable, NonFatal}
import scala.collection.mutable

final class PinnedActorThreadPool(
                                   val minThreads: Int,
                                   val maxThreads: Int,
                                   val keepAlive: FiniteDuration,
                                   threadFactory: PinnedActorThreadFactory,
                                 ) extends Shutdown {
  enum State {
    case Working(threadsCount: Int, lastAcquire: Long) extends State
    case Stopped extends State
  }
  import State.*
  private val state = AtomicReference[State](Working(minThreads, 0l))
  private val idleThreads = ConcurrentLinkedQueue[PinnedActorThread]()
  {
    var i = 0
    while(i < minThreads) {
      val thread = threadFactory.makeThread(this)
      idleThreads.offer(thread)
      thread.start()
      i += 1
    }
  }
  private val keepAliveNs: Long = keepAlive.toNanos

  private def tooMuchThreads(): Nothing = throw RejectedExecutionException(s"Reached maximum amount of threads: $maxThreads.")

  private def stoppedPoll(): Nothing = throw IllegalStateException("Pool is shutdown.")

  @tailrec
  def acquireThread[T](action: => T): Unit =
    state.get() match {
      case snapshot @ Working(threadsCount, lastAcquire) =>
        idleThreads.poll() match {
          case null =>
            if (threadsCount < maxThreads) {
              val updated = Working(threadsCount + 1, System.nanoTime())
              if(state.compareAndSet(snapshot, updated)) {
                val thread = threadFactory.makeThread(this)
                thread.giveWork(() => action)
                thread.start()
              } else acquireThread(action)
            } else tooMuchThreads()
          case thread =>
            thread.giveWork(() => action)
        }
      case Stopped => stoppedPoll()
    }

  @tailrec
  def watchExternal(): Unit =
    state.get() match {
      case snapshot @ Working(threadsCount, lastAcquire) =>
        if(threadsCount < maxThreads) {
          val updated = Working(threadsCount + 1, System.nanoTime)
          if(!state.compareAndSet(snapshot, updated)) watchExternal()
        } else tooMuchThreads()
      case Stopped => stoppedPoll()
    }

  @tailrec
  def forgetExternal(): Unit =
    state.get() match {
      case snapshot @ Working(threadsCount, lastAcquire) =>
        val updated = Working(threadsCount - 1, lastAcquire)
        if(!state.compareAndSet(snapshot, updated)) forgetExternal()
      case Stopped => ()
    }

  private[concurrent] def lostThread(thread: PinnedActorThread): Unit =
    forgetExternal()

  private[concurrent] def releaseThread(thread: PinnedActorThread): Unit =
    state.get() match {
      case snapshot @ Working(threadsCount, lastAcquire) =>
        val deadline = lastAcquire + keepAliveNs
        if(deadline <= System.nanoTime && threadsCount > minThreads) {
          thread.shutdown()
          @tailrec
          def loop(snapshot: Working): Unit = {
            val updated = Working(snapshot.threadsCount - 1, snapshot.lastAcquire)
            if(!state.compareAndSet(snapshot, updated)) {
              state.get() match {
                case snapshot: Working => loop(snapshot)
                case Stopped => ()
              }
            }
          }
          loop(snapshot)
        } else {
          idleThreads.offer(thread)
        }
      case Stopped => ()
    }

  override def shutdown(): Unit = state.set(Stopped)

}
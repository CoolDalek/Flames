package flames.concurrent

import java.util.concurrent.{ConcurrentLinkedQueue, RejectedExecutionException}
import java.util.concurrent.atomic.{AtomicInteger, AtomicLong}
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
  private val threadsCount = AtomicInteger(minThreads)
  private val idleThreads = ConcurrentLinkedQueue[PinnedActorThread]()
  private val lastAcquire = AtomicLong(System.nanoTime())
  @volatile private var stopped = false

  {
    var i = 0
    while(i < minThreads) {
      val thread = threadFactory.makeThread(this)
      idleThreads.offer(thread)
      thread.start()
      i += 1
    }
  }

  private def tooMuchThreads(): Nothing = throw RejectedExecutionException(s"Reached maximum amount of threads: $maxThreads.")

  private def stoppedPoll(): Nothing = throw IllegalStateException("Pool is shutdown.")

  private def updateTimer(): Unit = {
    val now = System.nanoTime()
    @tailrec
    def loop(): Unit = {
      val current = lastAcquire.get()
      if(now > current) {
        val success = lastAcquire.compareAndSet(current, now)
        if(!success) loop()
      }
    }
    loop()
  }

  def acquireThread[T](action: => T): Unit =
    idleThreads.poll() match {
      case null =>
        @tailrec
        def loop(): Unit = {
          val count = threadsCount.get()
          if(count < maxThreads) {
            val continue = threadsCount.compareAndSet(count, count + 1)
            if(continue) {
              if(stopped) {
                stoppedPoll()
              } else {
                val thread = threadFactory.makeThread(this)
                thread.giveWork(() => action)
                thread.start()
                updateTimer()
              }
            } else loop()
          } else tooMuchThreads()
        }
        loop()
      case thread =>
        thread.giveWork(() => action)
    }

  @tailrec
  def watchExternal(): Unit =
    if(stopped) {
      stoppedPoll()
    } else {
      val count = threadsCount.get()
      if(count < maxThreads) {
        if(!threadsCount.compareAndSet(count, count + 1)) watchExternal()
      } else tooMuchThreads()
    }

  def forgetExternal(): Unit = threadsCount.decrementAndGet()

  private[concurrent] def lostThread(thread: PinnedActorThread): Unit =
    threadsCount.decrementAndGet()

  private[concurrent] def releaseThread(thread: PinnedActorThread): Unit =
    if(stopped) {
      thread.shutdown()
    } else {
      val count = threadsCount.get()
      val deadline = lastAcquire.get() + keepAlive.toNanos
      if(deadline > System.nanoTime && count == minThreads) {
        idleThreads.offer(thread)
      } else {
        thread.shutdown()
      }
    }

  override def shutdown(): Unit = stopped = true

}
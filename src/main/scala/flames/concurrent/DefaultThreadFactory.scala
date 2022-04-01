package flames.concurrent

import java.lang.Thread.UncaughtExceptionHandler
import java.util.concurrent.{ForkJoinPool, ForkJoinWorkerThread, ThreadFactory}
import java.util.concurrent.atomic.AtomicInteger

class DefaultThreadFactory(
                            namePrefix: String,
                            reporter: UncaughtExceptionHandler,
                            priority: Int = Thread.NORM_PRIORITY,
                            initCount: Int = 0
                          ) extends ForkJoinPool.ForkJoinWorkerThreadFactory with ThreadFactory with PinnedActorThreadFactory {
  private val count = AtomicInteger(initCount)

  private def configure[T <: Thread](instance: T): T = {
    instance.setName(s"$namePrefix-${count.getAndIncrement}")
    instance.setDaemon(true)
    instance.setUncaughtExceptionHandler(reporter)
    instance.setPriority(priority)
    instance
  }

  private class FjkThread(pool: ForkJoinPool) extends ForkJoinWorkerThread(pool)

  override def newThread(pool: ForkJoinPool): ForkJoinWorkerThread =
    configure(FjkThread(pool))

  override def newThread(r: Runnable): Thread =
    configure(Thread(r))

  override def makeThread(pool: PinnedActorThreadPool): PinnedActorThread =
    configure(PinnedActorThread(pool))
  
}
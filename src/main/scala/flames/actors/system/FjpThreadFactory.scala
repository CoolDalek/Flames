package flames.actors.system

import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory
import java.util.concurrent.{ForkJoinPool, ForkJoinWorkerThread, Semaphore}
import scala.concurrent.{BlockContext, CanAwait}

class FjpThreadFactory(
  prefix: String,
  reporter: Thread.UncaughtExceptionHandler,
  maxBlocking: Int,
) extends CountingThreadFactory(
  prefix,
  Thread.NORM_PRIORITY,
  reporter,
), ForkJoinWorkerThreadFactory:
  private val blockers = Semaphore(maxBlocking)

  private class Worker(pool: ForkJoinPool) extends ForkJoinWorkerThread(pool), BlockContext:
    private var blocked = false

    override def blockOn[T](thunk: => T)(implicit permission: CanAwait): T =
      if Thread.currentThread() == this && !blocked && blockers.tryAcquire() then
        try
          blocked = true
          var done = false
          var result = null.asInstanceOf[T]
          val blocking = new ForkJoinPool.ManagedBlocker:
            override def block(): Boolean =
              if !done then
                result = thunk
                done = true
              end if
              done
            end block
            override def isReleasable: Boolean = done
          end blocking
          ForkJoinPool.managedBlock(blocking)
          result
        finally
          blocked = false
          blockers.release()
        end try
      else thunk
    end blockOn
  end Worker

  override def newThread(pool: ForkJoinPool): ForkJoinWorkerThread =
    configure(new Worker(pool))

end FjpThreadFactory

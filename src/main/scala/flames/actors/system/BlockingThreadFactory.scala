package flames.actors.system

import scala.concurrent.{BlockContext, CanAwait}

class BlockingThreadFactory(
  prefix: String,
  reporter: Thread.UncaughtExceptionHandler,
) extends CountingThreadFactory(
  prefix,
  Thread.NORM_PRIORITY,
  reporter,
):

  private class Worker(run: Runnable) extends Thread(run), BlockContext:
    override def blockOn[T](thunk: => T)(implicit permission: CanAwait): T = thunk
  end Worker

  override def newThread(run: Runnable): Thread =
    configure(Worker(run))

end BlockingThreadFactory

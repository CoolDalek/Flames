package flames.actors.system

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

open class CountingThreadFactory(
  prefix: String,
  priority: Int,
  reporter: Thread.UncaughtExceptionHandler,
) extends ThreadFactory {
  private val counter: AtomicInteger = new AtomicInteger(1)

  protected final def configure[T <: Thread](thread: T): T = {
    val id = counter.incrementAndGet()
    thread.setName(s"$prefix-$id")
    thread.setDaemon(true)
    thread.setUncaughtExceptionHandler(reporter)
    thread
  }

  override def newThread(run: Runnable): Thread =
    configure(new Thread(run))

}

package flames.concurrent

import java.util.UUID
import java.util.concurrent.atomic.AtomicReference
import scala.util.control.{ControlThrowable, NonFatal}
import ProcessState.*

final class PinnedActorThread(pool: PinnedActorThreadPool) extends Thread with Shutdown {

  private var work: Runnable = null.asInstanceOf[Runnable]
  private val state = AtomicReference(Idle)
  private val lock = new AnyRef() {}

  def giveWork(run: Runnable): Unit =
    if(state.compareAndSet(Idle, Running)) {
      work = run
      lock.synchronized(lock.notify())
    } else throw IllegalStateException("Trying to give work to already working thread.")

  private def waitWork(): Unit = {
    pool.releaseThread(this)
    lock.synchronized(lock.wait())
  }

  override def run(): Unit = {
    var loop = true
    while(loop) {
      state.get() match {
        case Stop =>
          loop = false
        case Running =>
          try {
            work.run()
            state.set(Idle)
            waitWork()
          } catch {
            case NonFatal(_) | _: ControlThrowable => ()
            case _: InterruptedException =>
              state.set(Stop)
              pool.lostThread(this)
              loop = false
          }
        case Idle =>
          waitWork()
      }
    }
  }

  override def shutdown(): Unit = state.set(Stop)

}
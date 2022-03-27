package flames.concurrent

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicReference
import scala.util.control.NonFatal

trait WorkerExecutor[T] {

  def tell(message: T): Unit

}
object WorkerExecutor {

  private class DefaultExecutor[T](act: Behavior[T], runtime: WorkersRuntime) extends WorkerExecutor[T] {
    import flames.concurrent.Behavior.*

    private val behavior: AtomicReference[Behavior[T]] = AtomicReference(act)
    private val messages = ConcurrentLinkedQueue[T]()

    override def tell(message: T): Unit =
      behavior.get() match {
        case Pass =>
          messages.offer(message)
        case _: Receive[T] =>
          val run = messages.isEmpty
          messages.offer(message)
          if(run) process()
        case Stop =>
          runtime.reportFailure(Undelivered(message))
      }

    private def process(): Unit =
      runtime.execute {
        if(!messages.isEmpty) {
          behavior.get() match {
            case receive: Receive[T] =>
              val continue = behavior.compareAndSet(receive, Pass)
              if(continue) {
                val message = messages.poll()
                runtime.execute {
                  try {
                    receive.act(message) match {
                      case Pass =>
                        behavior.set(receive)
                      case next =>
                        behavior.set(next)
                    }
                    process()
                  } catch {
                    case NonFatal(exc) =>
                      runtime.reportFailure(exc)
                      behavior.set(Stop)
                  }
                }
              }
            case Stop =>
              runtime.reportFailure(WorkerDead) // Most likely cannot happen
            case Pass => ()
          }
        }
      }

  }

  type Factory = [T] => (Behavior[T], WorkersRuntime) => WorkerExecutor[T]

  def defaultFactory: Factory = [T] => (b: Behavior[T], r: WorkersRuntime) => new DefaultExecutor[T](b, r)

}
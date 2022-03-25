package flames.concurrent

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicReference
import scala.util.control.NonFatal

//All commented inlines must be uncommented when Scala3 will have more stable inlining
trait Worker[T](using val runtime: WorkersRuntime) {

  sealed trait Behavior
  private case object Pass extends Behavior
  private case class Receive(act: T => Behavior) extends Behavior
  private case object Stop extends Behavior

  extension (behavior: Behavior) {

    inline private def handleWith(handler: Throwable => Behavior): Behavior =
      /*inline*/ behavior match {
        case Receive(act) =>
          receive { message =>
            try {
              act(message)
            } catch {
              case NonFatal(exc) =>
                handler(exc)
            }
          }
        case noop => noop
      }

    /*inline*/ def onFailure(handle: PartialFunction[Throwable, Behavior]): Behavior =
      handleWith { exc =>
        if(handle.isDefinedAt(exc)) handle(exc)
        else throw exc
      }

    /*inline*/ def onError(handle: Throwable => Behavior): Behavior =
      handleWith(handle)

  }

  private val behavior = AtomicReference(act())

  private val messages = ConcurrentLinkedQueue[T]()

  def tell(message: T): Unit =
    behavior.get match {
      case Pass =>
        messages.offer(message)
      case _: Receive =>
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
          case receive @ Receive(act) =>
            val continue = behavior.compareAndSet(receive, Pass)
            if(continue) {
              val message = messages.poll()
              runtime.execute {
                try {
                  act(message) match {
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

  /*inline*/ protected final def receive(receiver: T => Behavior): Behavior = Receive(receiver)

  inline protected final def stop: Behavior = Stop

  inline protected final def same: Behavior = Pass

  def act(): Behavior

}
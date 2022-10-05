package flames.concurrent.execution

import flames.concurrent.exception.*
import Execution.Continuation

trait Fiber[+T] extends Runnable, Continuation:
  import Fiber.*

  protected def runtime: ConcurrentRuntime
  protected final val execution = runtime.execution(this)
  protected final val state = runtime.atomic[State](Running)

  execution.continue()

  def join(callback: Stopped[T] => Unit): Cancellable

  def cancel(): Stopped[T]

  def id: Long

  def parent: Fiber[Nothing] | Null

  def childs: Set[Fiber[Nothing]]

object Fiber:

  sealed trait State
  case object Suspended extends State
  case object Running extends State
  sealed trait Stopped[+T] extends State
  case object Cancelled extends NoStackTrace("Cancelled"), Stopped[Nothing]
  case class Failed(reason: Throwable) extends OnlyReasonException(reason), Stopped[Nothing]


end Fiber

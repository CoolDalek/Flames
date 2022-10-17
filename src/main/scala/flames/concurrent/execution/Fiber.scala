package flames.concurrent.execution

import flames.concurrent.exception.*
import Execution.Continuation
import flames.concurrent.execution.atomic.AtomicRef

trait Fiber[+T](private val runtime: ConcurrentRuntime):
  import Fiber.*

  protected val state: AtomicRef[State] =
    runtime.atomicFactory.ref[State](New)

  def start(): Unit

  def join(callback: Result[T] => Unit): Cancellable

  def join(using TimeStealer): Result[T]

  def id: Long

  def parent: Fiber[Nothing] | Null

  def childs: Set[Fiber[Nothing]]

object Fiber:

  sealed trait State
  case object New extends State
  case object Suspended extends State
  case object Running extends State
  sealed trait Result[+T] extends State
  case class Done[T](value: T) extends Result[T]
  case class Failed(reason: Throwable) extends OnlyReasonException(reason), Result[Nothing]

end Fiber

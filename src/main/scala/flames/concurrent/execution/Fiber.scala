package flames.concurrent.execution

import flames.concurrent.exception.*
import Execution.Continuation
import flames.concurrent.execution.atomic.AtomicRef

import scala.annotation.tailrec

trait Fiber[+T](private val runtime: ConcurrentRuntime):
  import Fiber.*

  protected val state: AtomicRef[State] =
    runtime.atomicFactory.ref[State](New)

  def start(): Unit

  def join(callback: Result[T] => Unit): Cancellable

  final def join(waitForStart: Boolean = false)(using time: TimeStealer): Result[T] =
    var idleIterations = 0
    @tailrec
    def loop(): Result[T] =
      state.get match
        case New if !waitForStart => Failed(IllegalStateException("Fiber not started yet"))
        case result: Result[?] => result.asInstanceOf[Result[T]]
        case _ =>
          if(time.steal()) idleIterations = 0
          else idleIterations += 1
          if(idleIterations >= time.idleThreshold) Thread.onSpinWait()
          loop()
    end loop
    loop()
  end join

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

  val thread = Thread.currentThread()
  thread.isInterrupted

end Fiber

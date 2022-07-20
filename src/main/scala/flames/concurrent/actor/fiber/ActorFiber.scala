package flames.concurrent.actor.fiber

import flames.concurrent.execution.*
import flames.concurrent.execution.ProcessState.*
import flames.util.Nullable.*

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicReference
import scala.annotation.{switch, tailrec}
import scala.collection.mutable
import scala.util.control.NonFatal
import flames.concurrent.actor.*
import flames.concurrent.actor.mailbox.*
import flames.concurrent.actor.behavior.*
import flames.concurrent.actor.behavior.BehaviorTag.*
import flames.logging.FailureReporter
import ActorFiber.*

import scala.concurrent.ExecutionContext

final class ActorFiber[T](
                           private val state: FiberState[T],
                           private var behavior: Behavior[T],
                           private val reporter: FailureReporter,
                           private val runtime: ActorRuntime,
                           executionFactory: ExecutionStrategy.Factory,
                         ) extends Runnable {
  import state.procState
  export state.path

  private val execution = executionFactory(executionLoop _)

  override def run(): Unit = {
    procState.set(Running)
    execution.run()
  }

  def addChild(actor: ActorRef[Nothing]): Unit =
    state.addChild(actor.path, actor)

  def removeChild(path: ActorPath[Nothing]): Option[ActorRef[Nothing]] =
    state.removeChild(path)

  def getChilds: Set[ActorRef[Nothing]] =
    state.getChilds

  private def stopCleanup(): Unit =
    getChilds.foreach(_.silentStop())

  private def reportStop(reason: StopReason): Unit =
    import state.parent
    if(parent == ActorParent.root) {
      runtime.removeRootChild(path)
    } else {
      val nn = parent.asInstanceOf[ActorRef[Nothing]]
      nn.systemTell(
        SystemMessage.ChildStopped(
          path,
          null,
          reason,
        )
      )
    }
  end reportStop

  @tailrec
  def stop(silent: Boolean): Unit = {
    def report(): Unit =
      if(!silent) reportStop(StopReason.Shutdown)

    (procState.get(): @switch) match {
      case Idle =>
        if (procState.compareAndSet(Idle, Stop)) {
          report()
          stopCleanup()
        } else stop(silent)
      case Running =>
        report()
        procState.set(Stop)
      case Stop => ()
    }
  }

  private[concurrent] final def userTell(message: T): Unit =
    tell(message)(state.put)

  private[concurrent] final def timerTell(message: T): Unit =
    tell(message)(state.timerPut)

  private[concurrent] final def systemTell(message: SystemMessage): Unit =
    tell(message)(state.systemPut)

  private def tell[R](msg: R)(put: R => Unit): Unit =
    (procState.get(): @switch) match {
      case Stop =>
        reporter.reportFailure(Undelivered(msg), path)
      case Running =>
        put(msg)
      case Idle =>
        put(msg)
        execution.continue()
    }

  protected final def executionLoop(): Unit =
    import state.*
    prepare()
    acquire()
    while (loop) {
      if (yieldCount > 0 && System.nanoTime() < deadline) {
        processSystem {
          processUser {
            release()
            execution.sleep()
          }
        }
      } else {
        release()
        execution.`yield`()
      }
    }
  end executionLoop

  inline private def processSystem(inline onEmpty: => Unit): Unit =
    receiveMessage(state.systemMail, onEmpty) { message =>
      import flames.concurrent.actor.mailbox.SystemTag.*
      import flames.concurrent.actor.mailbox.SystemMessage.*
      message.tag match {
        case ChildStoppedTag =>
          val stopped = message.asInstanceOf[ChildStopped]
          removeChild(stopped.childToken).foreach { ref =>
            stopped.childRef = ref
            if(behavior.tag == ReceiveTag) {
              act(_.actSystem, stopped)
            }
          }
      }
    }

  //TODO: Benchmark with and without inlining
  inline def act[R](inline getter: Behavior.Receive[T] => GenericAct[T, R], inline msg: R): Unit =
    val receive = behavior.asInstanceOf[Behavior.Receive[T]]
    getter(receive).notNull { act =>
      val next = act(msg)
      (next.tag: @switch) match {
        case SameTag =>
          behavior = receive
        case _ =>
          behavior = next
      }
    }
  end act

  inline private def processUser(inline onEmpty: => Unit): Unit =
    receiveMessage(state.userMail, onEmpty) { message =>
      act(_.act, message)
    }

  private def receiveMessage[R](mailbox: Mailbox[R], onEmpty: => Unit)
                               (process: R => Unit): Unit =
    val result = if (procState.get() != Stop) {
      mailbox.poll() match {
        case null =>
          EmptyQueue
        case message =>
          (behavior.tag: @switch) match {
            case ReceiveTag =>
              try {
                process(message.asInstanceOf[R])
              } catch {
                case NonFatal(exc) =>
                  reportStop(
                    StopReason.Failure(exc)
                  )
                  reporter.reportFailure(exc, path)
                  behavior = Behavior.Stop
              }
            case _ => ()
          }
          (behavior.tag: @switch) match {
            case StopTag =>
              procState.set(Stop)
              Break
            case _ =>
              Continue
          }
      }
    } else Break
    (result: @switch) match {
      case EmptyQueue =>
        onEmpty
      case Continue =>
        state.yieldCount -= 1
      case Break =>
        stopCleanup()
        state.loop = false
    }
  end receiveMessage

}
object ActorFiber {

  trait Factory {

    def apply[T](
                  name: String,
                  behavior:Behavior[T],
                  parent: ActorParent,
                  runtime: ActorRuntime,
                  model: ExecutionModel,
                ): ActorFiber[T]

  }

  val defaultFactory: Factory = new Factory {

    override def apply[T](
                           name: String,
                           behavior: Behavior[T],
                           parent: ActorParent,
                           runtime: ActorRuntime,
                           model: ExecutionModel,
                         ): ActorFiber[T] =
      val path = runtime.pathFactory[T](name, parent)
      val post = runtime.mailboxFactory[T](model, runtime.config)
      val state = FiberState[T](
        post,
        runtime.config,
        parent,
        path,
      )
      val execution = model match {
        case ExecutionModel.Async =>
          ShiftedExecution[T](runtime, state)
        case ExecutionModel.Blocking =>
          ShiftedExecution[T](runtime.blockingEC, state)
        case ExecutionModel.Pinned =>
          PinnedExecution[T](runtime.blocking, state)
      }
      new ActorFiber[T](
        state = state,
        behavior = behavior,
        reporter = runtime,
        runtime = runtime,
        executionFactory = execution,
      )
    end apply

  }

  type ProcessResult = EmptyQueue | Continue | Break
  type EmptyQueue = 1
  inline val EmptyQueue: EmptyQueue = 1
  type Continue = 2
  inline val Continue: Continue = 2
  type Break = 3
  inline val Break: Break = 3
}
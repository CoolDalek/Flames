package flames.actors.fiber

import flames.actors
import flames.actors.*
import flames.actors.behavior.Behavior
import flames.actors.behavior.Behavior.*
import flames.actors.fiber.ProcessResult.*
import flames.actors.fiber.State.*
import flames.actors.message.*
import flames.actors.message.SystemMessage.*
import flames.actors.path.ActorPath
import flames.actors.utils.*

import java.util.concurrent.atomic.AtomicReference
import scala.collection.mutable
import scala.util.control.NonFatal

class Fiber[T](
  private var behavior: Behavior[T],
  private val mailbox: Mailbox[T],
  private val children: Children,
  private val autoYield: Int,
  val parent: Parent,
  val system: ActorSystem,
  val path: ActorPath,
) extends AtomicReference[State](Idle) with Runnable {

  export children.{
    add as addChild,
    values as getChildren,
  }

  private val watchers = mutable.Set.empty[ErasedRef]

  override def run(): Unit = {
    //There is no way State is not Running,
    // but we need to synchronize the actor's data,
    // so we just pretend to work here.
    if (get() == Running) executionLoop()
  }

  private inline def scheduleRun(): Unit =
    system.execute(this)

  private def executionLoop(): Unit =
    var loop = true
    var yieldCount = autoYield
    while (loop) {
      if (yieldCount > 0) {

        def processMail[R](poll: => R | Null, process: R => ProcessResult)(onEmpty: => Unit): Unit =
          val msg = poll
          if (null == msg) onEmpty else
            process(msg.asInstanceOf[R]) match
              case Continue =>
                yieldCount -= 1
              case Break =>
                val reason = StopReason.Shutdown
                set(Stopped(reason))
                loop = false
                reportStop(reason)
            end match
          end if
        end processMail

        try
          processMail(mailbox.pollInternal(), processInternal) {
            processMail(mailbox.pollProtocol(), processProtocol) {
              if (mailbox.isEmpty)
                set(Idle)
                if (mailbox.isEmpty)
                  loop = false
                else
                  val continue = compareAndSet(Idle, Running)
                  if (!continue) loop = false
            }
          }
        catch {
          case NonFatal(exc) =>
            val reason = StopReason.Failure(exc)
            set(Stopped(reason))
            loop = false
            reportStop(reason)
        }

      } else {
        loop = false
        //Change Running from Running just for synchronization.
        set(Running)
        scheduleRun()
      }
    }
  end executionLoop

  private def act[R](get: Receive[T] => R => Behavior[T], msg: R): ProcessResult =
    behavior match
      case Stop => Break
      case Same => Continue
      case receive: Receive[T] =>
        val act = get(receive)
        act(msg) match
          case Stop =>
            behavior = Stop
            Break
          case Same =>
            Continue
          case receive: Receive[T] =>
            behavior = receive
            Continue
  end act

  private def processProtocol(msg: T): ProcessResult =
    act(_.actProtocol, msg)

  private def processSystem(msg: SystemMessage): ProcessResult =
    act(_.actSystem, msg)

  private def processInternal(msg: InternalMessage): ProcessResult =
    msg match
      case cs @ ChildStopped(path, _) =>
        val ref = children.remove(path)
        if (null == ref) Continue
        else processSystem(cs)
      case ps: ParentStopped =>
        processSystem(ps)
        Break
      case noop @ (WatchedStopped(_, _) | CantWatch(_, _) | CantUnwatch(_, _)) =>
        processSystem(noop)
      case WatchRequest(ref) =>
        watchers.add(ref)
        Continue
      case UnwatchRequest(ref) =>
        watchers.remove(ref)
        Continue
      case FindChild(selector, index, replyTo) =>
        import flames.actors.path.Selector.Protocol.*
        val set = children.search(selector(index))
        val next = index + 1
        if (selector.length > next)
          if (set.isEmpty) replyTo.tell(NoResults(path))
          else {
            val request = FindChild(selector, next, replyTo)
            set.foreach(_.internalTell(request))
            replyTo.tell(Reroute(path, set))
          }
        else if (set.isEmpty) replyTo.tell(NoResults(path))
        else replyTo.tell(Result(path, set))
        Continue
  end processInternal

  private def tell[R](msg: R, push: R => Boolean): Ack[Unit] =
    get() match
      case Stopped(_) =>
        Ack.Undelivered(DeliveryFailure.DeadLetter)
      case Idle =>
        if (push(msg)) {
          val run = compareAndSet(Idle, Running)
          if (run) scheduleRun()
          Ack.Ok
        } else Ack.Overflow
      case Running =>
        if (push(msg)) Ack.Ok
        else Ack.Overflow
  end tell

  def timerTell(msg: T): Ack[Unit] = tell(msg, mailbox.pushTimer)

  def userTell(msg: T): Ack[Unit] = tell(msg, mailbox.pushUser)

  def internalTell(msg: InternalMessage): Ack[Unit] = tell(msg, mailbox.pushInternal)

  private def reportStop(reason: StopReason): Unit =
    watchers.foreach { ref =>
      ref.internalTell(
        WatchedStopped(path, reason),
      )
    }
    parent.notNull { ref =>
      ref.internalTell(
        ChildStopped(path, reason),
      )
    }

    def deadLetter[R](msg: R): Unit =
      system.deadLetters.publish(
        msg,
        path,
        DeliveryFailure.DeadLetter,
      )

    mailbox.drainInternal(deadLetter)
    mailbox.drainProtocol(deadLetter)
  end reportStop

}
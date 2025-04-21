package flames.actors.ref

import flames.actors.*
import flames.actors.fiber.{Fiber, State}
import flames.actors.message.*
import flames.actors.message.Ack.*
import flames.actors.message.DeliveryFailure.*
import flames.actors.message.SystemMessage.InternalMessage
import flames.actors.path.ActorPath
import flames.actors.pattern.*

import scala.reflect.{ClassTag, classTag}

class LocalRef[T](
  val fiber: Fiber[T],
  val tag: Class[?],
) extends ActorRef[T]:
  export fiber.{
    getChildren,
    addChild,
    path,
  }

  private def safeTell[R](msg: R, tell: R => Ack[Unit]): Unit =
    tell(msg) match
      case Undelivered(reason) =>
        fiber.system.deadLetters.publish(msg, path, reason)
      case _: Delivered[Unit] => ()
  end safeTell

  override def tell(msg: T): Unit =
    safeTell(msg, fiber.userTell)

  override private[actors] def timerTell(msg: T): Unit =
    safeTell(msg, fiber.timerTell)

  override private[actors] def internalTell(msg: InternalMessage): Unit =
    safeTell(msg, fiber.internalTell)

  override def ackTell[F[_]: Wait](msg: T): F[Ack[Unit]] =
    Wait[F].pure(fiber.userTell(msg))

  override def ask[F[_]: Wait, Response](request: ActorRef[Response] => T)(using
    timeout: Timeout,
  ): F[Ack[Response]] =
    Wait[F].asyncAck[Response] { callback =>
      fiber.system.spawnRef {
        new Question[Response](callback, timeout)
      }.map { ref =>
        tell(request(ref))
      }
    }

  override private[actors] def watchRequest[R](ref: ActorRef[R]): Unit =
    fiber.internalTell(SystemMessage.WatchRequest(ref)) match
      case Undelivered(reason) =>
        val reply = if (reason == DeliveryFailure.DeadLetter)
          val stopReason = ref match
            case ref: LocalRef[_] =>
              ref.fiber.get() match
                case State.Stopped(reason) => reason
                case _ => StopReason.Unknown // Shouldn't happen
            case _ => StopReason.Unknown
          end stopReason
          SystemMessage.WatchedStopped(fiber.path, stopReason)
        else SystemMessage.CantWatch(fiber.path, reason)
        ref.internalTell(reply)
      case _: Delivered[Unit] => ()
  end watchRequest

  override private[actors] def unwatchRequest[R](ref: ActorRef[R]): Unit =
    fiber.internalTell(SystemMessage.UnwatchRequest(ref)) match
      case Undelivered(reason) =>
        if (reason != DeliveryFailure.DeadLetter)
          ref.internalTell(
            SystemMessage.CantUnwatch(fiber.path, reason),
          )
      case _: Delivered[Unit] => ()
  end unwatchRequest

  def spawn[A: ClassTag, B <: Actor[A]](actor: ActorEnv[A] ?=> B): B =
    given ActorSystem = fiber.system

    val env = ActorEnv.make[A](this)
    val instance = actor(using env)
    fiber.addChild(instance.selfRef)
    instance
  end spawn

end LocalRef

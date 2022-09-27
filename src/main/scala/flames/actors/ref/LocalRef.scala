package flames.actors.ref

import flames.actors.fiber.Fiber
import flames.actors.message.*
import SystemMessage.InternalMessage
import flames.actors.pattern.Wait
import flames.actors.*
import Ack.*
import DeliveryFailure.*
import flames.actors.path.ActorPath

import scala.reflect.{ClassTag, classTag}

class LocalRef[T](
                   fiber: Fiber[T],
                   val tag: Class[?],
                 ) extends ActorRef[T] {
  export fiber.{
    getChilds,
    addChild,
    path,
  }

  private def safeTell[R](msg: R, tell: R => Ack[Unit]): Unit =
    tell(msg) match
      case Undelivered(reason) =>
        fiber.system.deadLetter.publish(msg, path, reason)
      case _: Delivered[Unit] => ()
  end safeTell

  override def tell(msg: T): Unit =
    safeTell(msg, fiber.userTell)

  override private[actors] def timerTell(msg: T): Unit =
    safeTell(msg, fiber.timerTell)

  override private[actors] def internalTell(msg: InternalMessage): Unit =
    safeTell(msg, fiber.internalTell)

  override def ackTell[F[_] : Wait](msg: T): F[Ack[Unit]] =
    Wait[F].lift(fiber.userTell(msg))

  override def ask[F[_] : Wait, Response](request: ActorRef[Response] => T)(using Timeout): F[Ack[Response]] = ???

  override private[actors] def watchRequest[R](ref: ActorRef[R]): Unit =
    fiber.internalTell(SystemMessage.WatchRequest(ref)) match
      case Undelivered(reason) =>
        val reply = if(reason == DeadLetter)
          SystemMessage.WatchedStopped(fiber.path, StopReason.Unknown)
        else
          SystemMessage.CantWatch(fiber.path, reason)
        ref.internalTell(reply)
      case _: Delivered[Unit] => ()
  end watchRequest

  override private[actors] def unwatchRequest[R](ref: ActorRef[R]): Unit =
    fiber.internalTell(SystemMessage.UnwatchRequest(ref)) match
      case Undelivered(reason) =>
        if(reason != DeadLetter)
          ref.internalTell(
            SystemMessage.CantUnwatch(fiber.path, reason)
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

}

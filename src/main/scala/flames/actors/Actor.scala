package flames.actors

import flames.actors.message.*
import flames.actors.path.*
import flames.actors.pattern.Wait
import flames.actors.ref.LocalRef
import flames.actors.system.{ActorConfig, Cancellable}

import scala.concurrent.duration.FiniteDuration
import scala.reflect.ClassTag

trait Actor[T](using env: ActorEnv[T])(name: String, config: ActorConfig = env.system.config):
  import behavior.Builder

  export Builder.{ReceiveProtocol, ReceiveSystem}
  export behavior.Behavior
  export flames.actors.message.{Ack, Timeout, SystemMessage}

  protected given system: ActorSystem = env.system

  private[actors] val selfRef: LocalRef[T] =
    system.refProvider.local(
      name,
      act(),
      config,
    )

  protected def act(): Behavior[T]

  protected[actors] final def self: ActorRef[T] = selfRef

  inline protected def receive(inline receive: WithState[T => Behavior[T]]): ReceiveProtocol[T] =
    Builder.Receive(receive)

  inline protected def receiveSystem(inline receive: WithState[SystemMessage => Behavior[T]]): ReceiveSystem[T] =
    Builder.Receive(receive)

  inline protected def same: Behavior[T] = Behavior.Same

  inline protected def stop: Behavior[T] = Behavior.Stop

  inline protected def scheduleToSelf(delay: FiniteDuration, message: T): Cancellable =
    system.scheduleMessage(delay, selfRef, message)

  inline protected def scheduleToSelf(delay: FiniteDuration, period: FiniteDuration, message: T): Cancellable =
    system.scheduleMessage(delay, period, selfRef, message)

  protected def spawn[A: ClassTag, B <: Actor[A]](actor: ActorEnv[A] ?=> B)(using StateAccess): (B, ActorRef[A]) = {
    val instance = spawnObj(actor)
    instance -> instance.selfRef
  }

  inline protected def spawnFire[A: ClassTag, B <: Actor[A]](actor: ActorEnv[A] ?=> B)(using StateAccess): Unit =
    spawnObj(actor)

  protected def spawnRef[A: ClassTag, B <: Actor[A]](actor: ActorEnv[A] ?=> B)(using StateAccess): ActorRef[A] =
    spawnObj(actor).selfRef

  protected def spawnObj[A: ClassTag, B <: Actor[A]](actor: ActorEnv[A] ?=> B)(using StateAccess): B =
    selfRef.spawn(actor)

  protected def children(using StateAccess): Set[ActorRef[Nothing]] = selfRef.getChildren

  protected def selectChildren[F[_]: Wait, A: ClassTag](query: Vector[ActorSelector])(using Timeout): F[SelectionResult[A]] =
    system.selector.selectFrom(selfRef, query)

  protected def watch[A](ref: ActorRef[A]): Unit =
    ref.watchRequest(selfRef)

  protected def unwatch[A](ref: ActorRef[A]): Unit =
    ref.unwatchRequest(selfRef)

end Actor

package flames.actors

import flames.actors.fiber.Childs
import flames.actors.path.*
import flames.actors.ref.LocalRef
import flames.actors.system.Cancellable
import flames.actors.message.*
import flames.actors.pattern.Wait
import utils.*

import scala.concurrent.duration.FiniteDuration
import scala.reflect.ClassTag

trait Actor[T](name: String)(using ActorEnv[T]) {

  import flames.actors
  import actors.behavior
  import behavior.Builder
  export Builder.{ReceiveProtocol, ReceiveSystem}
  export behavior.Behavior
  export actors.message.{Ack, Timeout, SystemMessage}

  protected given system: ActorSystem = ActorEnv.system[T]

  private[actors] val selfRef: LocalRef[T] =
    system.makeRef(
      name,
      act(),
      Mailbox.concurrentLinkedQueue[T],
      Childs.sync,
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

  protected def spawn[T: ClassTag, R <: Actor[T]](actor: ActorEnv[T] ?=> R)(using StateAccess): (R, ActorRef[T]) = {
    val instance = spawnObj(actor)
    instance -> instance.selfRef
  }

  inline protected def spawnFire[T: ClassTag, R <: Actor[T]](actor: ActorEnv[T] ?=> R)(using StateAccess): Unit =
    spawnObj(actor)

  protected def spawnRef[T: ClassTag, R <: Actor[T]](actor: ActorEnv[T] ?=> R)(using StateAccess): ActorRef[T] =
    spawnObj(actor).selfRef

  protected def spawnObj[T: ClassTag, R <: Actor[T]](actor: ActorEnv[T] ?=> R)(using StateAccess): R =
    selfRef.spawn(actor)
  
  protected def childs(using StateAccess): Set[ActorRef[Nothing]] = selfRef.getChilds
  
  protected def selectChilds[F[_]: Wait, T: ClassTag](query: Vector[ActorSelector])(using Timeout): F[SelectionResult[T]] =
    system.selector.selectFrom(selfRef, query)

  protected def watch[R](ref: ActorRef[R]): Unit =
    ref.watchRequest(selfRef)

  protected def unwatch[R](ref: ActorRef[R]): Unit =
    ref.unwatchRequest(selfRef)

}

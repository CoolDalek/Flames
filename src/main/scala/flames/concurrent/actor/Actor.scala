package flames.concurrent.actor

import flames.concurrent.actor.fiber.*
import ActorType.*
import flames.concurrent.actor.behavior.*
import flames.concurrent.actor.mailbox.SystemMessage
import flames.logging.Logger

import scala.compiletime.*
import scala.annotation.threadUnsafe

trait Actor[T, Type <: ActorType](using ActorEnv, ValueOf[Type]) {

  protected val runtime: ActorRuntime = ActorEnv.runtime

  @threadUnsafe
  private[actor] lazy val fiber: ActorFiber[T] =
    valueOf[Type] match {
      case Blocking =>
        runtime.makeBlocking[T](
          act(),
          ActorEnv.parent
        )
      case Pinned =>
        runtime.makePinned[T](
          act(),
          ActorEnv.parent
        )
      case Async =>
        runtime.makeAsync[T](
          act(),
          ActorEnv.parent
        )
    }

  protected[actor] final val self: ActorRef[T] = new ActorRef[T] {

    override def tell(message: T): Unit =
      fiber.userTell(message)

    override private[concurrent] def timerTell(message: T): Unit =
      fiber.timerTell(message)

    override private[concurrent] def systemTell(message: SystemMessage): Unit =
      fiber.systemTell(message)

    override private[concurrent] def silentStop(): Unit =
      fiber.stop(true)

    override def stop(): Unit =
      fiber.stop(false)

    override def token: ActorToken =
      fiber.token

  }

  sealed trait StateAccess

  protected final def childs(using StateAccess): Set[ActorRef[Nothing]] = fiber.getChilds

  protected final def spawn[R](factory: ActorFactory[R])(using StateAccess): ActorRef[R] = {
    val child = factory(using ActorEnv.withParent(self))
    val ref = child.self
    fiber.addChild(ref)
    ref
  }

  inline protected def receive(inline act: StateAccess ?=> T => Behavior[T]): ReceiveProtocol[T] = {
    given StateAccess = new StateAccess {}
    BehaviorBuilder.Receive(act)
  }
  
  inline protected def receiveSystem(inline act: StateAccess ?=> SystemMessage => Behavior[T]): ReceiveSystem[T] = {
    given StateAccess = new StateAccess {}
    BehaviorBuilder.Receive(act)
  }

  inline protected def same: Behavior[T] = Behavior.Same

  inline protected def stop: Behavior[T] = Behavior.Stop
  
  inline protected def logger: Logger = runtime.logger

  def act(): Behavior[T]

}
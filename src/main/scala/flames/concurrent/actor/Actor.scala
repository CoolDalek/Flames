package flames.concurrent.actor

import flames.concurrent.actor.fiber.*
import ActorType.*
import flames.concurrent.actor.mailbox.SystemMessage

import scala.compiletime.*
import scala.annotation.threadUnsafe
import scala.language.postfixOps

type ActorFactory[T] = ActorRuntime ?=> Actor[T]
trait Actor[T](using val runtime: ActorRuntime) {
  type Type <: ActorType

  @threadUnsafe
  private lazy val fiber: ActorFiber[T] =
    valueOf[Type] match {
      case Blocking => ???
      case Pinned => ???
      case Async => ???
    }
    
  def token: ActorToken = fiber.token

  protected final val self: ActorRef[T] = new ActorRef[T] {

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
      
  }

  sealed trait StateAccess

  protected final def childs(using StateAccess): Set[ActorRef[Nothing]] = fiber.getChilds

  protected final def spawn[R](factory: ActorFactory[R])(using StateAccess): ActorRef[R] = {
    val child = factory(using runtime)
    val ref = child.self
    fiber.addChild(
      child.token,
      ref,
    )
    ref
  }

  inline protected def receive[T](inline act: StateAccess ?=> T => Behavior[T]): Behavior[T] = {
    given StateAccess = new StateAccess {}
    Behavior.Receive(act)
  }

  inline protected def same: Behavior[T] = Behavior.Same

  inline protected def stop: Behavior[T] = Behavior.Stop

  def act(): Behavior[T]

}
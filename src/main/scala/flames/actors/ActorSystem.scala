package flames.actors

import flames.actors.behavior.*
import flames.actors.fiber.*
import flames.actors.message.*
import flames.actors.path.*
import flames.actors.pattern.*
import flames.actors.ref.*
import flames.actors.system.*
import flames.actors.utils.*

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import scala.reflect.{ClassTag, classTag}

trait ActorSystem(
  name: String,
  unique: Unique,
) extends ExecutionContext {
  protected given ActorSystem = this

  def deployment: Deployment

  def root: ErasedRef = deployment.root.selfRef

  def path: ActorPath = root.path

  def deadLetters: DeadLetters = deployment.deadLetters

  def selector: Selector = deployment.selector

  def spawn[F[_]: Wait, T: ClassTag, R <: Actor[T]](actor: ActorEnv[T] ?=> R): F[(R, ActorRef[T])] =
    spawnObj[F, T, R](actor).map(x => x -> x.self)

  def spawnForget[F[_]: Wait, T: ClassTag, R <: Actor[T]](actor: ActorEnv[T] ?=> R): F[Unit] =
    spawnObj[F, T, R](actor).void

  def spawnRef[F[_]: Wait, T: ClassTag, R <: Actor[T]](actor: ActorEnv[T] ?=> R): F[ActorRef[T]] =
    spawnObj[F, T, R](actor).map(_.self)

  def spawnObj[F[_]: Wait, T: ClassTag, R <: Actor[T]](actor: ActorEnv[T] ?=> R): F[R] =
    import Ack.*
    Wait[F].asyncAck[R] { callback =>
      deployment.root.selfRef.tell(
        Root.Spawn(actor, callback, classTag[T]),
      )
    }.flatMap {
      case Delivered(value) => Wait[F].pure(value)
      // Shouldn't happen
      case Undelivered(failure) => Wait[F].raise(failure)
    }
  end spawnObj

  def scheduleMessage[T](delay: FiniteDuration, to: ActorRef[T], message: T): Cancellable

  def scheduleMessage[T](delay: FiniteDuration, period: FiniteDuration, to: ActorRef[T], message: T): Cancellable

  private[actors] def makeRef[T](
    name: String,
    behavior: Behavior[T],
    mailbox: Mailbox[T],
    children: Children,
  )(using env: ActorEnv[T]): LocalRef[T] =
    val parent = env.parent
    val path = parent.mapOrElse(
      x => ActorPath.child(x.path, name),
      ActorPath.local(name, this.path.unique),
    )
    val fiber = Fiber[T](
      behavior = behavior,
      mailbox = mailbox,
      system = this,
      children = children,
      parent = parent,
      path = path,
    )
    LocalRef[T](
      fiber,
      env.tag.runtimeClass,
    )
  end makeRef

}

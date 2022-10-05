package flames.concurrent.actors

import flames.concurrent.actors.behavior.*
import flames.concurrent.actors.fiber.*
import flames.concurrent.actors.message.*
import flames.concurrent.actors.path.*
import flames.concurrent.actors.pattern.*
import flames.concurrent.actors.system.*
import flames.concurrent.utils.*
import flames.concurrent.actors.ref.*
import flames.concurrent.actors.path.Unique
import flames.concurrent.actors.system.Cancellable

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import scala.reflect.{ClassTag, classTag}

trait ActorSystem(
                   name: String,
                   unique: Unique,
                   makeDeployment: ActorSystem => Deployment,
                 ) extends ExecutionContext {
  protected given ActorSystem = this

  def root: ErasedRef = deployment.root.selfRef

  def path: ActorPath = root.path

  final val deployment: Deployment = makeDeployment(this)
  
  export deployment.{deadLetter, selector}

  def spawn[F[_] : Wait, T: ClassTag, R <: Actor[T]](actor: ActorEnv[T] ?=> R): F[(R, ActorRef[T])] =
    spawnObj[F, T, R](actor).map(x => x -> x.self)

  def spawnFire[F[_] : Wait, T: ClassTag, R <: Actor[T]](actor: ActorEnv[T] ?=> R): F[Unit] =
    spawnObj[F, T, R](actor).map(_ => ())

  def spawnRef[F[_] : Wait, T: ClassTag, R <: Actor[T]](actor: ActorEnv[T] ?=> R): F[ActorRef[T]] =
    spawnObj[F, T, R](actor).map(_.self)

  def spawnObj[F[_] : Wait, T: ClassTag, R <: Actor[T]](actor: ActorEnv[T] ?=> R): F[R] =
    import Ack.*
    Wait[F].asyncAck[R] { callback =>
      deployment.root.selfRef.tell(
        deployment.root.makeSpawn(actor, callback)
      )
    }.map {
      case Delivered(value) => value
      case Undelivered(failure) => throw failure // If this happens we have really serious problems
    }
  end spawnObj

  def scheduleMessage[T](delay: FiniteDuration, to: ActorRef[T], message: T): Cancellable

  def scheduleMessage[T](delay: FiniteDuration, period: FiniteDuration, to: ActorRef[T], message: T): Cancellable

  private[actors] def makeRef[T](
                                  name: String,
                                  behavior: Behavior[T],
                                  mailbox: Mailbox[T],
                                  childs: Childs,
                                )(using ActorEnv[T]): LocalRef[T] =
    val parent = ActorEnv.parent[T]
    val path = parent.mapOrElse(
      x => ActorPath.child(x.path, name),
      ActorPath.local(name, this.path.unique)
    )
    val fiber = Fiber[T](
      behavior = behavior,
      mailbox = mailbox,
      system = this,
      childs = childs,
      parent = parent,
      path = path,
    )
    LocalRef[T](
      fiber,
      ActorEnv.tag[T].runtimeClass,
    )
  end makeRef

}

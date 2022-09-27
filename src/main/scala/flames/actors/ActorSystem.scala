package flames.actors

import flames.actors.behavior.*
import flames.actors.fiber.*
import flames.actors.message.*
import flames.actors.path.*
import flames.actors.pattern.*
import flames.actors.system.*
import flames.actors.utils.*
import flames.actors.ref.*

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import scala.reflect.{ClassTag, classTag}

trait ActorSystem(
                   name: String,
                   unique: Unique,
                 ) extends ExecutionContext {
  protected given ActorSystem = this

  def path: ActorPath

  def root: ErasedRef = deployment.root.selfRef

  def deadLetter: DeadLetter = deployment.deadLetter

  def deployment: Deployment

  inline def spawn[F[_] : Wait]: Spawner.All[F] = Spawner.All[F](deployment.root)

  def spawnFire[F[_] : Wait]: Spawner.Fire[F] = Spawner.Fire[F](deployment.root)

  inline def spawnRef[F[_] : Wait]: Spawner.Ref[F] = Spawner.Ref[F](deployment.root)

  inline def spawnObj[F[_] : Wait]: Spawner.Obj[F] = Spawner.Obj[F](deployment.root)

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

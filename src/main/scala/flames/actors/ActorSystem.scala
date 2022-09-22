package flames.actors

import flames.actors.behavior.*
import flames.actors.fiber.*
import flames.actors.message.*
import flames.actors.path.*
import flames.actors.pattern.*
import flames.actors.system.*
import flames.actors.utils.Nulls.*
import flames.actors.ref.*

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import scala.reflect.{ClassTag, classTag}

trait ActorSystem(
                   name: String,
                   unique: Unique,
                   val deadLetter: DeadLetter,
                 ) extends ExecutionContext {
  self =>

  protected given ActorSystem = self

  protected val root = new Root(name)(using ActorEnv.root[Root.Protocol])

  inline def spawn[F[_] : Wait]: Spawner.All[F] = Spawner.All[F](root)

  inline def spawnFire[F[_] : Wait]: Spawner.Fire[F] = Spawner.Fire[F](root)

  inline def spawnRef[F[_] : Wait]: Spawner.Ref[F] = Spawner.Ref[F](root)

  inline def spawnObj[F[_] : Wait]: Spawner.Obj[F] = Spawner.Obj[F](root)
/*
  def selectErased[F[_] : Wait](selector: Vector[ActorSelector])(using timeout: Timeout): F[SelectionResult[Nothing]] =
    import flames.actors.path.SelectionResult.*

    def validRoot: Boolean = {
      val rootSelector = selector(0)
      val namesEqual = rootSelector.name == path.name
      val uniquesEqual = rootSelector.unique.mapOrElse(
        x => x == path.unique,
        true
      )
      namesEqual && uniquesEqual
    }

    if (selector.size > 1 && validRoot)
      val set = childs.search(selector.head)
      if (selector.size == 2) {
        val result = SelectionResult.make(set)
        Wait[F].delivered(result)
      } else Wait[F].async { callback =>
        spawnFire {
          SelectorCombiner(
            set,
            selector,
            callback,
            timeout,
          )
        }
      }
    else Wait[F].delivered(NotFound)
  end selectErased

  def select[T: ClassTag, F[_] : Wait](selector: Vector[ActorSelector])(using Timeout): F[SelectionResult[T]] =
    import flames.actors.path.SelectionResult.*

    def checkTypes(ref: ActorRef[Nothing]): Boolean =
      ref.tag.isAssignableFrom(
        classTag[T].runtimeClass
      )

    def cast(ref: ActorRef[Nothing]): ActorRef[T] = ref.asInstanceOf[ActorRef[T]]

    selectErased[F](selector).map {
      case NotFound => NotFound
      case FoundOne(ref) =>
        if (checkTypes(ref)) FoundOne(cast(ref))
        else NotFound
      case FoundMany(set) =>
        val filtered = set.collect {
          case ref if checkTypes(ref) => cast(ref)
        }
        SelectionResult.make(filtered)
    }
  end select
*/
  def scheduleMessage[T](delay: FiniteDuration, to: ActorRef[T], message: T): Cancellable

  def scheduleMessage[T](delay: FiniteDuration, period: FiniteDuration, to: ActorRef[T], message: T): Cancellable

  final val path: ActorPath = ActorPath.root(name, unique)

  private[actors] def makeRef[T](
                                  name: String,
                                  behavior: Behavior[T],
                                  mailbox: Mailbox[T],
                                  childs: Childs,
                                )(using ActorEnv[T]): LocalRef[T] =
    val parent = ActorEnv.parent[T]
    val path = parent.mapOrElse(
      x => ActorPath.child(x.path, name),
      ActorPath.root(name, self.path.unique)
    )
    val fiber = Fiber[T](
      behavior = behavior,
      mailbox = mailbox,
      system = self,
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

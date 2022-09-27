package flames.actors.path

import flames.actors.*
import flames.actors.message.*
import SystemMessage.*
import Ack.*
import flames.actors.behavior.Behavior
import flames.actors.path.Selector.Protocol
import Protocol.*
import flames.actors.pattern.Wait
import flames.actors.ref.*

import scala.collection.mutable
import scala.reflect.{ClassTag, classTag}

type SelectorRef = ActorRef[Protocol]

trait Selector {

  def select[F[_]: Wait, T: ClassTag](query: Vector[ActorSelector])(using Timeout): F[SelectionResult[T]]

  def selectLocal[F[_]: Wait, T: ClassTag](query: Vector[ActorSelector])(using Timeout): F[SelectionResult[T]]

  def selectRemote[F[_]: Wait, T: ClassTag](query: Vector[ActorSelector])(using Timeout): F[SelectionResult[T]]

  def selectFrom[F[_]: Wait, T: ClassTag](start: ErasedRef, query: Vector[ActorSelector])(using Timeout): F[SelectionResult[T]]

}
object Selector {


  class Impl(system: ActorSystem) extends Selector:

    inline private def noResults[F[_]: Wait, T]: F[SelectionResult[T]] = Wait[F].lift(SelectionResult.NotFound)

    private def selectLocalImpl[F[_]: Wait, T: ClassTag](query: Vector[ActorSelector], startWith: Int, root: ErasedRef)
                                                        (using timeout: Timeout): F[SelectionResult[T]] =
      Wait[F].async[SelectionResult[Nothing]] { callback =>
        system.spawnFire[F][Protocol, Combiner] { (env: ActorEnv[Protocol]) ?=>
          new Combiner(
            query,
            callback,
            timeout,
            startWith,
            root,
          )(using env)
        }
      }.map(filterTypes[T])
    end selectLocalImpl

    private def selectRemoteImpl[F[_]: Wait, T: ClassTag](query: Vector[ActorSelector],
                                                          credentials: ActorSelector.Remote)
                                                         (using Timeout): F[SelectionResult[T]] = ???

    private def filterTypes[T: ClassTag](result: SelectionResult[Nothing]): SelectionResult[T] =
      import SelectionResult.*

      def checkTypes(ref: ActorRef[Nothing]): Boolean =
        ref.tag.isAssignableFrom(
          classTag[T].runtimeClass
        )

      def cast(ref: ActorRef[Nothing]): ActorRef[T] = ref.asInstanceOf[ActorRef[T]]

      result match {
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
    end filterTypes

    inline private def nonEmpty[F[_]: Wait, T](query: Vector[ActorSelector])
                                              (inline select: => F[SelectionResult[T]]): F[SelectionResult[T]] =
      if(query.length > 1)
        select
      else noResults[F, T]
    end nonEmpty

    override def select[F[_] : Wait, T: ClassTag](query: Vector[ActorSelector])
                                                 (using Timeout): F[SelectionResult[T]] =

      def localOr(orElse: => F[SelectionResult[T]]): F[SelectionResult[T]] =
        if (query(1).matches(system.path))
          if (query.length > 1)
            selectLocalImpl[F, T](query, 1, system.root)
          else Wait[F].lift {
            filterTypes[T](SelectionResult.FoundOne(system.root))
          }
        else orElse
      end localOr

      nonEmpty[F, T](query) {
        import ActorSelector.*
        query(1) match
          case _: Simple =>
            system.path match
              case _: (ActorPath.Local | ActorPath.Child) =>
                localOr(noResults[F, T])
              case _: ActorPath.Remote => noResults[F, T]
          case credentials: Remote =>
            system.path match
              case _: ActorPath.Remote =>
                localOr {
                  selectRemoteImpl[F, T](query, credentials)
                }
              case _: (ActorPath.Local | ActorPath.Child) => noResults[F, T]
      }
    end select

    override def selectLocal[F[_] : Wait, T: ClassTag](query: Vector[ActorSelector])(using timeout: Timeout): F[SelectionResult[T]] =
      nonEmpty[F, T](query) {
        import ActorSelector.*

        def select: F[SelectionResult[T]] =
          if (query.length > 1)
            selectLocalImpl(query, 1, system.root)
          else noResults[F, T]

        query.head match {
          case head: Simple =>
            if(head.matches(system.path))
              select
            else selectLocalImpl(query, 0, system.root)
          case head: Remote =>
            if (head.matches(system.path))
              select
            else noResults[F, T]
        }
      }
    end selectLocal

    override def selectRemote[F[_] : Wait, T: ClassTag](query: Vector[ActorSelector])(using Timeout): F[SelectionResult[T]] =
      nonEmpty[F, T](query) {
        import ActorSelector.*
        query.head match
          case head: Simple =>
            noResults[F, T]
          case head: Remote =>
            selectRemoteImpl(query, head)
      }
    end selectRemote

    override def selectFrom[F[_] : Wait, T: ClassTag](start: ErasedRef, query: Vector[ActorSelector])(using Timeout): F[SelectionResult[T]] = ???

    class Combiner(
                    query: Vector[ActorSelector],
                    complete: SelectionResult[Nothing] => Unit,
                    timeout: Timeout,
                    startWith: Int,
                    root: ErasedRef,
                  )(using ActorEnv[Protocol]) extends Actor[Protocol]("selector-combiner"):

      private val builder = Set.newBuilder[ErasedRef]
      private val waitOn = mutable.Map.empty[ActorPath, ErasedRef]

      private def dontWait(path: ActorPath): Unit =
        waitOn.remove(path).foreach(unwatch)

      private def tryComplete(): Behavior[Protocol] =
        if (waitOn.isEmpty)
          val set = builder.result()
          val result = SelectionResult.make(set)
          complete(result)
          stop
        else same
      end tryComplete

      def act(): Behavior[Protocol] =
        scheduleToSelf(timeout.asDuration, NoTimeLeft)
        val request = SystemMessage.FindChild(query, startWith, self)
        root.internalTell(request)
        watch(root)
        waitOn.update(root.path, root)
        receive {
          case Result(from, set) =>
            dontWait(from)
            builder.addAll(set)
            tryComplete()
          case Reroute(from, to) =>
            dontWait(from)
            to.foreach { ref =>
              watch(ref)
              waitOn.update(ref.path, ref)
            }
            same
          case NoResults(from) =>
            dontWait(from)
            tryComplete()
          case NoTimeLeft =>
            complete(SelectionResult.NotFound)
            waitOn.foreach(x => unwatch(x._2))
            stop
        } and receiveSystem {
          case WatchedStopped(path, _) =>
            waitOn.remove(path)
            tryComplete()
          case CantWatch(path, _) =>
            waitOn.remove(path)
            tryComplete()
          case _ => same
        }
      end act

    end Combiner

  end Impl

  enum Protocol {
    case Reroute(from: ActorPath, to: Set[ErasedRef])
    case Result(from: ActorPath, set: Set[ErasedRef])
    case NoResults(from: ActorPath)
    case NoTimeLeft
  }

}
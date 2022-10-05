package flames.concurrent.actors.path

import flames.concurrent.actors.*
import flames.concurrent.actors.message.*
import flames.concurrent.actors.behavior.Behavior
import flames.concurrent.actors.path.Selector.Protocol
import flames.concurrent.actors.pattern.Wait
import flames.concurrent.actors.ref.*
import flames.concurrent.actors.remote.Client
import flames.concurrent.actors.ActorRef
import flames.concurrent.actors.remote.Client

import scala.collection.mutable
import scala.reflect.{ClassTag, classTag}

type SelectorRef = ActorRef[Protocol]

trait Selector {

  def select[F[_]: Wait, T: ClassTag](query: Vector[ActorSelector])
                                     (using Timeout): F[SelectionResult[T]]

  private[actors] def remoteRequest[F[_]: Wait](query: Vector[ActorSelector])
                                               (using Timeout): F[SelectionResult[Nothing]]

  def selectLocal[F[_]: Wait, T: ClassTag](query: Vector[ActorSelector])
                                          (using Timeout): F[SelectionResult[T]]

  def selectRemote[F[_]: Wait, T: ClassTag](query: Vector[ActorSelector])
                                           (using Timeout): F[Ack[SelectionResult[T]]]

  def selectFrom[F[_]: Wait, T: ClassTag](start: ErasedRef, query: Vector[ActorSelector])
                                         (using Timeout): F[SelectionResult[T]]

}
object Selector {
/*
  def apply(system: ActorSystem, client: Client): Selector = new Impl(system, client)

  private class Impl(system: ActorSystem, client: Client) extends Selector:

    private def localErased[F[_]: Wait](query: Vector[ActorSelector], startWith: Int, root: ErasedRef)
                                       (using timeout: Timeout): F[SelectionResult[Nothing]] =
      Wait[F].async[SelectionResult[Nothing]] { callback =>
        system.spawnFire {
          new Combiner(
            query,
            callback,
            timeout,
            startWith,
            root,
          )
        }
      }
    end localErased

    def remoteRequest[F[_]: Wait](query: Vector[ActorSelector])(using Timeout): F[SelectionResult[Nothing]] =
      if(query.head.matches(system.path))
        if(query.length > 1) localErased(query, 1, system.root)
        else Wait[F].lift(SelectionResult.FoundOne(system.root))
      else noResults[F, Nothing]
    end remoteRequest

    inline private def noResults[F[_]: Wait, T]: F[SelectionResult[T]] = Wait[F].lift(SelectionResult.NotFound)
    inline private def noResultsAck[F[_]: Wait, T]: F[Ack[SelectionResult[T]]] = Wait[F].lift(Ack.Delivered(SelectionResult.NotFound))

    private def selectLocalImpl[F[_]: Wait, T: ClassTag](query: Vector[ActorSelector], startWith: Int, root: ErasedRef)
                                                        (using Timeout): F[SelectionResult[T]] =
      localErased[F](query, startWith, root).map(filterTypes[T])
    end selectLocalImpl

    private def selectRemoteImpl[F[_]: Wait, T: ClassTag](query: Vector[ActorSelector],
                                                          credentials: ActorSelector.Remote)
                                                         (using Timeout): F[Ack[SelectionResult[T]]] =
      import Ack.*
      client.select(query, credentials).map {
        case Delivered(result) => Delivered(filterTypes[T](result))
        case failure => failure.asInstanceOf[Ack[SelectionResult[T]]]
      }
    end selectRemoteImpl

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
          SelectionResults.make(filtered)
      }
    end filterTypes

    inline private def nonEmpty[F[_]: Wait, T](query: Vector[ActorSelector])
                                              (inline select: => F[SelectionResult[T]]): F[SelectionResult[T]] =
      if(query.length > 1)
        select
      else noResults[F, T]
    end nonEmpty

    inline private def nonEmptyAck[F[_]: Wait, T](query: Vector[ActorSelector])
                                                 (inline select: => F[Ack[SelectionResult[T]]]): F[Ack[SelectionResult[T]]] =
      if(query.length > 1)
        select
      else noResultsAck[F, T]
    end nonEmptyAck

    override def select[F[_] : Wait, T: ClassTag](query: Vector[ActorSelector])
                                                 (using Timeout): F[Ack[SelectionResult[T]]] =

      def localOr(orElse: => F[Ack[SelectionResult[T]]]): F[Ack[SelectionResult[T]]] =
        if (query(1).matches(system.path))
          val local = if (query.length > 1)
            selectLocalImpl[F, T](query, 1, system.root)
          else Wait[F].lift {
            filterTypes[T](SelectionResult.FoundOne(system.root))
          }
          local.map(Ack.Delivered.apply)
        else orElse
      end localOr

      nonEmptyAck[F, T](query) {
        import ActorSelector.*
        query(1) match
          case _: Simple =>
            system.path match
              case _: (ActorPath.Local | ActorPath.Child) =>
                localOr(noResultsAck[F, T])
              case _: ActorPath.Remote => noResultsAck[F, T]
          case credentials: Remote =>
            system.path match
              case _: ActorPath.Remote =>
                localOr {
                  selectRemoteImpl[F, T](query, credentials)
                }
              case _: (ActorPath.Local | ActorPath.Child) => noResultsAck[F, T]
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

    override def selectRemote[F[_]: Wait, T: ClassTag](query: Vector[ActorSelector])(using Timeout): F[Ack[SelectionResult[T]]] =
      nonEmptyAck[F, T](query) {
        import ActorSelector.*
        query.head match
          case _: Simple =>
            noResultsAck[F, T]
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

      import Protocol.*
      import SystemMessage.*

      private val builder = Set.newBuilder[ErasedRef]
      private val waitOn = mutable.Map.empty[ActorPath, ErasedRef]

      private def dontWait(path: ActorPath): Unit =
        waitOn.remove(path).foreach(unwatch)

      private def tryComplete(): Behavior[Protocol] =
        if (waitOn.isEmpty)
          val set = builder.result()
          val result = SelectionResults.make(set)
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
*/
  enum Protocol {
    case Reroute(from: ActorPath, to: Set[ErasedRef])
    case Result(from: ActorPath, set: Set[ErasedRef])
    case NoResults(from: ActorPath)
    case NoTimeLeft
  }

}
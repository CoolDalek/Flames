package flames.actors.path

import flames.actors.*
import flames.actors.behavior.Behavior
import flames.actors.message.*
import flames.actors.message.Ack.*
import flames.actors.message.SystemMessage.*
import flames.actors.path.Selector.Protocol
import flames.actors.path.Selector.Protocol.*
import flames.actors.pattern.Wait
import flames.actors.ref.*
import flames.actors.remote.Client

import scala.collection.mutable
import scala.reflect.{ClassTag, classTag}

type SelectorRef = ActorRef[Selector.Protocol]

trait Selector {

  def select[F[_]: Wait, T: ClassTag](
    query: Vector[ActorSelector],
  )(using Timeout): F[SelectionResult[T]]

  def selectFrom[F[_]: Wait, T: ClassTag](
    start: ErasedRef,
    query: Vector[ActorSelector],
  )(using Timeout): F[SelectionResult[T]]

  def selectRemote[F[_]: Wait, T: ClassTag](
    host: String,
    port: Int,
    query: Vector[ActorSelector],
  )(using Timeout): F[Ack[SelectionResult[T]]]

}
object Selector {

  enum Protocol {
    case Reroute(from: ActorPath, to: Set[ErasedRef])
    case Result(from: ActorPath, set: Set[ErasedRef])
    case NoResults(from: ActorPath)
  }

  def apply(system: ActorSystem, client: Client): Selector = new:

    inline private def noResults[F[_]: Wait, T]: F[SelectionResult[T]] =
      Wait[F].pure(SelectionResult.NotFound)

    private def filterTypes[T: ClassTag](result: SelectionResult[Nothing]): SelectionResult[T] =
      import SelectionResult.*

      def checkTypes(ref: ActorRef[Nothing]): Boolean =
        ref.tag.isAssignableFrom(
          classTag[T].runtimeClass,
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

    private def selectLocalImpl[F[_]: Wait, T: ClassTag](
      query: Vector[ActorSelector],
      startWith: Int,
      root: ErasedRef,
    )(using timeout: Timeout): F[SelectionResult[T]] =
      Wait[F].async[SelectionResult[Nothing]] { callback =>
        system.spawnForget {
          new Combiner(
            query,
            callback,
            timeout,
            startWith,
            root,
          )
        }
      }.map(filterTypes[T])

    inline private def nonEmpty[F[_]: Wait, T](
      query: Vector[ActorSelector],
    )(inline select: => F[SelectionResult[T]]): F[SelectionResult[T]] =
      if (query.length > 1)
        select
      else noResults[F, T]
    end nonEmpty

    override def select[F[_]: Wait, T: ClassTag](
      query: Vector[ActorSelector],
    )(using Timeout): F[SelectionResult[T]] =
      nonEmpty[F, T](query) {
        import ActorSelector.*
        if (query(1).matches(system.path))
          if (query.length > 1)
            selectLocalImpl[F, T](query, 1, system.root)
          else Wait[F].pure {
            filterTypes[T](SelectionResult.FoundOne(system.root))
          }
        else noResults[F, T]
      }
    end select

    override def selectFrom[F[_]: Wait, T: ClassTag](
      start: ErasedRef,
      query: Vector[ActorSelector],
    )(using Timeout): F[SelectionResult[T]] =
      nonEmpty(query) {
        selectLocalImpl(query, 0, start)
      }

    override def selectRemote[F[_]: Wait, T: ClassTag](
      host: String,
      port: Int,
      query: Vector[ActorSelector],
    )(using Timeout): F[Ack[SelectionResult[T]]] = {
      def noResultsAck: F[Ack[SelectionResult[T]]] =
        noResults.map(Ack.Delivered.apply)

      def callRemote(root: String) =
        client.select(
          host,
          port,
          root,
          query,
        ).map { ack =>
          ack.map(filterTypes[T])
        }

      system.path match {
        case ActorPath.Remote(_, _, localHost, localPort) if host == localHost && port == localPort =>
          query.headOption match {
            case Some(local) if local.matches(system.path) =>
              selectLocalImpl(query, 1, system.root).map(Ack.Delivered.apply)
            case Some(remote) =>
              callRemote(remote.name)
            case None => noResultsAck
          }
        case _ =>
          if query.isEmpty
          then noResultsAck
          else callRemote(query(1).name)
      }
    }

    private case object NoTimeLeft
    private type Protocol = NoTimeLeft.type | Selector.Protocol
    private class Combiner(
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
        waitOn.update(root.path, root)
        watch(root)
        root.internalTell(request)
        receive {
          case Result(from, set) =>
            dontWait(from)
            builder.addAll(set)
            tryComplete()
          case Reroute(from, to) =>
            dontWait(from)
            to.foreach { ref =>
              waitOn.update(ref.path, ref)
              watch(ref)
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

  end apply

}
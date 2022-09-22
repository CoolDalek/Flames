package flames.actors.path

import flames.actors.*
import flames.actors.message.*
import SystemMessage.*
import Ack.*
import flames.actors.behavior.Behavior
import flames.actors.path.Selector.Protocol
import Protocol.*
import flames.actors.collections.ActorRefSet
import flames.actors.ref.*
import flames.actors.utils.Nulls.*

import scala.collection.mutable

type SelectorRef = ActorRef[Protocol]
class Selector {


  class Combiner(
                  query: Vector[ActorSelector],
                  complete: DeliveryFailure | SelectionResult[Nothing] => Unit,
                  timeout: Timeout,
                  startWith: Int,
                  rootSet: Set[ErasedRef],
                )(using ActorEnv[Protocol]) extends Actor[Protocol]("selector-combiner") {

    private val builder = Set.newBuilder[ErasedRef]
    private val waitOn: ActorRefSet[Nothing] = ???

    private def dontWait(path: ActorPath): Unit =
      waitOn.remove(path).notNull(x => unwatch(x))

    private def tryComplete(): Behavior[Protocol] =
      if(waitOn.isEmpty)
        val set = builder.result()
        val result = SelectionResult.make(set)
        complete(result)
        stop
      else same
    end tryComplete

    def act(): Behavior[Protocol] =
      scheduleToSelf(timeout.asDuration, NoTimeLeft)
      val request = SystemMessage.FindChild(query, startWith, self)
      rootSet.foreach { ref =>
        ref.internalTell(request)
        watch(ref)
        waitOn.add(ref)
      }
      receive {
        case Result(from, set) =>
          dontWait(from)
          builder.addAll(set)
          tryComplete()
        case Reroute(from, to) =>
          dontWait(from)
          to.foreach { ref =>
            watch(ref)
            waitOn.add(ref)
          }
          same
        case NoResults(from) =>
          dontWait(from)
          tryComplete()
        case NoTimeLeft =>
          complete(DeliveryFailure.TimedOut)
          waitOn.foreach(unwatch)
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

  }

}
object Selector {

  enum Protocol {
    case Reroute(from: ActorPath, to: Set[ErasedRef])
    case Result(from: ActorPath, set: Set[ErasedRef])
    case NoResults(from: ActorPath)
    case NoTimeLeft
  }
  
}
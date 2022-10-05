package flames.concurrent.actors.message

import SystemMessage.InternalMessage
import flames.concurrent.actors.path.*
import flames.concurrent.actors.*

sealed trait SystemMessage extends InternalMessage

object SystemMessage {

  case class ChildStopped(path: ActorPath, reason: StopReason) extends SystemMessage
  case class ParentStopped(path: ActorPath, reason: StopReason) extends SystemMessage
  case class WatchedStopped(path: ActorPath, reason: StopReason) extends SystemMessage
  case class CantWatch(path: ActorPath, reason: DeliveryFailure) extends SystemMessage
  case class CantUnwatch(path: ActorPath, reason: DeliveryFailure) extends SystemMessage
  
  private[actors] sealed trait InternalMessage

  private[actors] case class FindChild(
                                        selector: Vector[ActorSelector],
                                        index: Int,
                                        replyTo: SelectorRef,
                                      ) extends InternalMessage
  private[actors] case class WatchRequest(ref: ErasedRef) extends InternalMessage
  private[actors] case class UnwatchRequest(ref: ErasedRef) extends InternalMessage
  
}
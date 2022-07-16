package flames.concurrent.actor.mailbox

import flames.concurrent.actor.*
import SystemMessage.*
import SystemTag.*

sealed trait SystemMessage {
  def tag: SystemTag
}
object SystemMessage {

  class ChildStopped(
                      private[actor] val childToken: ActorToken,
                      private[actor] var childRef: ActorRef[Nothing] | Null,
                      val reason: StopReason,
                    ) extends SystemMessage {
    inline private[actor] def nnChildRef: ActorRef[Nothing] = childRef.asInstanceOf[ActorRef[Nothing]]
    val tag: SystemTag = ChildStoppedTag
  }

  object ChildStopped {

    def unapply(x: SystemMessage): Option[(ActorRef[Nothing], StopReason)] =
      if(x.tag == ChildStoppedTag) {
        val self = x.asInstanceOf[ChildStopped]
        Some(
          self.nnChildRef -> self.reason
        )
      } else None

  }

}
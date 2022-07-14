package flames.concurrent.actor.mailbox

import flames.concurrent.actor.*
import SystemMessage.*
import SystemTag.*

sealed trait SystemMessage {
  def tag: SystemTag
}
object SystemMessage {

  class ChildStopped(
                      val child: ActorToken,
                      val reason: StopReason,
                    ) extends SystemMessage {
    val tag: SystemTag = ChildStoppedTag
  }

}
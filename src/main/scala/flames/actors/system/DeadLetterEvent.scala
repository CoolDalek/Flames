package flames.actors.system

import flames.actors.message.DeliveryFailure
import flames.actors.path.ActorPath

trait DeadLetterEvent {

  def message: Any

  def target: ActorPath

  def reason: DeliveryFailure

}
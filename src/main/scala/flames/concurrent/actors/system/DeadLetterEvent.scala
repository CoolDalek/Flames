package flames.concurrent.actors.system

import flames.concurrent.actors.message.DeliveryFailure
import flames.concurrent.actors.path.ActorPath

trait DeadLetterEvent {

  def message: Any

  def target: ActorPath

  def reason: DeliveryFailure

}
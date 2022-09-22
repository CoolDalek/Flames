package flames.actors.system

import flames.actors.message.DeliveryFailure
import flames.actors.path.ActorPath

trait DeadLetter {
  
  def publish[T](message: T, target: ActorPath, reason: DeliveryFailure): Unit

}

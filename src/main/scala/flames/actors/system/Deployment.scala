package flames.actors.system

import flames.actors.ActorRef
import flames.actors.path.Selector

trait Deployment {

  def `type`: Deployment.Type

  private[actors] def root: Root

  def deadLetter: DeadLetter

  def selector: Selector

}
object Deployment {

  enum Type {
    case Local
    case Remote
  }

}
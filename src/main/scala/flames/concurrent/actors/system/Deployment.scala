package flames.concurrent.actors.system

import flames.concurrent.actors.ActorRef
import flames.concurrent.actors.path.Selector
import flames.concurrent.actors.path.Selector

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
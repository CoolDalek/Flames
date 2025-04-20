package flames.actors.system

import flames.actors.ActorRef
import flames.actors.path.Selector

trait Deployment {

  def kind: Deployment.Kind

  def root: Root

  def deadLetters: DeadLetters

  def selector: Selector

}
object Deployment {

  enum Kind {
    case Local
    case Remote
  }

}
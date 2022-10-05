package flames.concurrent.actors.behavior

import flames.concurrent.actors.message.SystemMessage

enum Behavior[-T] {
  case Same extends Behavior[Any]
  case Receive[T](
                   actProtocol: T => Behavior[T],
                   actSystem: SystemMessage => Behavior[T],
                 ) extends Behavior[T]
  case Stop extends Behavior[Any]
}

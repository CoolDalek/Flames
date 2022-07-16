package flames.concurrent.actor.behavior

import flames.concurrent.actor.mailbox.SystemMessage

import scala.annotation.switch
import scala.util.control.NonFatal

import Behavior.*
import BehaviorTag.*

type GenericAct[T, R] = (R => Behavior[T]) | Null
type ActProtocol[T] = GenericAct[T, T]
type ActSystem[T] = GenericAct[T, SystemMessage]

sealed trait Behavior[+T] {
  def tag: BehaviorTag
}
object Behavior {

  object Same extends Behavior[Nothing] {
    override val tag: BehaviorTag = SameTag
  }

  final class Receive[T](
                          val act: ActProtocol[T],
                          val actSystem: ActSystem[T],
                        ) extends Behavior[T] {
    override val tag: BehaviorTag = ReceiveTag
  }
  
  object Stop extends Behavior[Nothing] {
    override val tag: BehaviorTag = StopTag
  }

}
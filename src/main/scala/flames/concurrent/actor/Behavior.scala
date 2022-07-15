package flames.concurrent.actor

import Behavior.*
import BehaviorTag.*

import scala.util.control.NonFatal
import scala.annotation.switch

sealed trait Behavior[+T] {
  private[actor] def tag: BehaviorTag
}
private[actor] object Behavior {

  object Same extends Behavior[Nothing] {
    override private[actor] val tag = SameTag
  }

  final class Receive[T](val act: T => Behavior[T]) extends Behavior[T] {
    override private[actor] val tag = ReceiveTag
  }

  object Receive {
    inline final def apply[T](act: T => Behavior[T]) = new Receive[T](act)
  }
  
  object Stop extends Behavior[Nothing] {
    override private[actor] val tag = StopTag
  }

}
extension [T](behavior: Behavior[T]) {

  //TODO: reimplement as macros
  inline private def handleWith(inline handler: Throwable => Behavior[T]): Behavior[T] = {
    (behavior.tag: @switch) match {
      case ReceiveTag =>
        val act = behavior.asInstanceOf[Receive[T]].act
        Receive[T]( msg =>
          try {
            act(msg)
          } catch {
            case NonFatal(exc) => handler(exc)
          }
        )
      case _ => behavior
    }
  }

  inline def onFailure(inline handler: PartialFunction[Throwable, Behavior[T]]): Behavior[T] =
    handleWith { exc =>
      // "orElse" branch in applyOrElse, for some reason, is not lazy
      if(handler.isDefinedAt(exc)) handler(exc)
      else throw exc
    }

  inline def onError(inline handler: Throwable => Behavior[T]): Behavior[T] =
    handleWith(handler)

}
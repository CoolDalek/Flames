package flames.concurrent

import scala.util.control.NonFatal

enum Behavior[+T] {
  case Same extends Behavior[Nothing]
  case Receive[T](act: T => Behavior[T]) extends Behavior[T]
  case Stop extends Behavior[Nothing]
}
import Behavior.*
object Behavior {
  inline def receive[T](inline act: T => Behavior[T]): Behavior[T] = Receive(act)

  inline def same: Behavior[Nothing] = Same

  inline def stop: Behavior[Nothing] = Stop
}
extension [T](behavior: Behavior[T]) {

  inline private def handleWith(inline handler: Throwable => Behavior[T]): Behavior[T] = {
    inline behavior match {
      case receive: Receive[T] =>
        inline val act = receive.act
        Receive[T]( msg =>
          try {
            act(msg)
          } catch {
            case NonFatal(exc) => handler(exc)
          }
        )
      case pass => pass
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
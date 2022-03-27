package flames.concurrent

import scala.util.control.NonFatal

enum Behavior[+T] {
  case Pass extends Behavior[Nothing]
  case Receive[T](act: T => Behavior[T]) extends Behavior[T]
  case Stop extends Behavior[Nothing]
}
import Behavior.*
extension [T](behavior: Behavior[T]) {

  inline def handleWith(inline handler: Throwable => Behavior[T]): Behavior[T] = {
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
      if(handler.isDefinedAt(exc)) handler(exc)
      else throw exc
    }

  inline def onError(inline handler: Throwable => Behavior[T]): Behavior[T] =
    handleWith(handler)

}
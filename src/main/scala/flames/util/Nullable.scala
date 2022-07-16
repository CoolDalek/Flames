package flames.util

import scala.util.NotGiven

object Nullable {

  extension [T](self: T | Null) {
    
    inline def orElse(inline other: => T | Null): T | Null =
      if(null == self) other else self

    inline def notNull[R](inline f: T => R)(using NotGiven[R =:= Unit]): R | Null =
      if(null == self) null else f(self.asInstanceOf[T])

    inline def notNull(inline f: T => Unit): Unit =
      if(null != self) f(self.asInstanceOf[T])
    
  }

}
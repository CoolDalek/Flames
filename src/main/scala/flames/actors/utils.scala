package flames.actors

import scala.util.NotGiven

object utils {

  extension[T] (self: T | Null) {

    inline def ifNull[R](inline orElse: => R): T | R =
      if (null == self) orElse
      else self.asInstanceOf[T]

    inline def notNull[R](inline map: T => R)(using NotGiven[R =:= Unit]): R | Null =
      if (null == self) null
      else map(self.asInstanceOf[T])

    inline def notNull(inline foreach: T => Unit): Unit =
      if (null != self) foreach(self.asInstanceOf[T])

    inline def mapOrElse[R](inline map: T => R, inline orElse: => R): R =
      if(null == self) orElse
      else map(self.asInstanceOf[T])

    inline def toOption: Option[T] =
      if (null == self) None
      else Some(self.asInstanceOf[T])

  }

}

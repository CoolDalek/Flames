package flames.concurrent.utils

import scala.util.NotGiven

opaque type Maybe[+T] = T
object Maybe:

  extension [T: Empty](self: Maybe[T]) {

    inline def ifEmpty[R](inline orElse: => R): T | R =
      if (self.isEmpty) orElse
      else self.asInstanceOf[T]

    inline def notEmpty[R: Empty](inline map: T => R)(using NotGiven[R =:= Unit]): Maybe[R] =
      if (self.isEmpty) Empty[R].empty
      else map(self)

    inline def notEmpty(inline foreach: T => Unit): Unit =
      if (self.nonEmpty) foreach(self)

    inline def mapOrElse[R](inline map: T => R, inline orElse: => R): R =
      if (self.nonEmpty) orElse
      else map(self)

  }

end Maybe
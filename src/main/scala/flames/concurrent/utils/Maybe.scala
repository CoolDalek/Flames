package flames.concurrent.utils

object Maybe:
  opaque type Maybe[+T] = T

  extension [T: Empty](self: Maybe[T]) {

    inline def get: T =
      if nonEmpty then self
      else throw new NoSuchElementException("Maybe.get")

    inline def isEmpty: Boolean = Empty[T].isEmpty(self)

    inline def nonEmpty: Boolean = Empty[T].nonEmpty(self)

    inline def ifAbsent[R](inline or: => R): T | R =
      if isEmpty then or
      else self

    inline def ifPresent[R: Empty](inline op: T => R)(using R =!= Unit): Maybe[R] =
      if nonEmpty then op(self)
      else Empty[R].empty
    // Distinguish cases when R is Unit and R is not Unit to avoid creating Empty instance for Unit
    inline def ifPresent(inline op: T => Unit): Unit =
      if nonEmpty then op(self)

    inline def fold[R](inline present: T => R, inline absent: => R): R =
      if isEmpty then absent
      else present(self)

    inline def toOption: Option[T] =
      if isEmpty then None
      else Some(self)

  }

  inline def absent[T: Empty]: Maybe[T] = Empty[T].empty

  inline def present[T: Empty](value: T): Maybe[T] = value

  object Absent:
    inline def apply[T: Empty]: Maybe[T] = Empty[T].empty
    inline def unapply[T: Empty](maybe: Maybe[T]): Boolean = maybe.isEmpty

  object Present:
    inline def apply[T: Empty](value: T): Maybe[T] = value
    inline def unapply[T: Empty](maybe: Maybe[T]): Maybe[T] = maybe

  inline given [T: Empty]: Conversion[T, Maybe[T]] = identity

export Maybe.{Maybe, Absent, Present}
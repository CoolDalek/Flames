package flames.actors.pattern

import flames.actors.utils.SummonerK

trait Monad[F[_]]:

  def pure[T](value: T): F[T]

  extension [T](self: F[T]) {

    def void: F[Unit] = map(_ => ())

    def as[R](value: R): F[R] = map(_ => value)

    def map[R](f: T => R): F[R]

    def flatMap[R](f: T => F[R]): F[R]

  }

object Monad extends SummonerK[Monad]

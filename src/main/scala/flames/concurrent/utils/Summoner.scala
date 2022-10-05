package flames.concurrent.utils

trait Summoner[Typeclass[_]] {

  inline def apply[T: Typeclass]: Typeclass[T] = summon[Typeclass[T]]

}

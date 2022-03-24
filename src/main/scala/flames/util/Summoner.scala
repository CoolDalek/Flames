package flames.util

trait Summoner[Typeclass[_]] {
  
  inline final def apply[T: Typeclass]: Typeclass[T] = summon[Typeclass[T]]

}
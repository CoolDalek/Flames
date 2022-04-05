package flames.util

trait Summoner[TypeClass[_]] {
  
  inline final def apply[T: TypeClass]: TypeClass[T] = summon[TypeClass[T]]

}
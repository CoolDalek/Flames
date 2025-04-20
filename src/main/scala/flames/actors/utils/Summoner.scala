package flames.actors.utils

trait Summoner[Typeclass[_]] {

  inline def apply[T](using ev: Typeclass[T]): ev.type = ev

}

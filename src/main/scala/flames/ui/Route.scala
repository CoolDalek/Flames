package flames.ui

import flames.util.Summoner

trait Route[T <: Screen] {

  private[ui] def make(context: UIContext): T

}
object Route {
  
  inline private[ui] def erased[T <: Screen : Route]: Route[Screen] = instance[T].asInstanceOf[Route[Screen]]

  inline private[ui] def instance[T <: Screen : Route]: Route[T] = summon[Route[T]]
  
  inline private[ui] def make[T <: Screen : Route](context: UIContext) = instance[T].make(context)

  inline def apply[T <: Screen](inline lambda: UIContext => T): Route[T] =
    new Route[T] {
      override private[ui] def make(context: UIContext) = lambda(context)
    }

}
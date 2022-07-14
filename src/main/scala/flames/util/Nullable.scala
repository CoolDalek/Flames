package flames.util

object Nullable {

  extension [T](self: T) {
    
    inline def orElse(inline other: => T): T =
      if(null == self) other else self
    
  }

}
package flames.util

object Nullable {

  extension [T](self: T | Null) {
    
    inline def orElse(inline other: => T | Null): T | Null =
      if(null == self) other else self
    
    inline def map[R](inline f: T => R): R | Null =
      if(null == self) null else f(self.asInstanceOf[T])
      
    inline def foreach(inline f: T => Unit): Unit =
      if(null != self) f(self.asInstanceOf[T])
    
  }

}
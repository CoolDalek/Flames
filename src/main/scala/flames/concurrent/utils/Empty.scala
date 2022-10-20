package flames.concurrent.utils

trait Empty[T]:

  def empty: T

  extension (self: T)

    def isEmpty: Boolean = empty == self

    def nonEmpty: Boolean = empty != self

  end extension
  
object Empty extends Summoner[Empty]:
  
  inline given [T <: AnyRef]: Empty[T] with
    inline override def empty: T = null.asInstanceOf[T]
    extension (self: T)
      inline override def isEmpty: Boolean = self eq null
      inline override def nonEmpty: Boolean = self ne null
    end extension
  end given
  
end Empty

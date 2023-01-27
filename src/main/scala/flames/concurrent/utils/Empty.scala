package flames.concurrent.utils

trait Empty[T]:
  def isEmpty(value: T): Boolean
  def nonEmpty(value: T): Boolean
  def empty: T

object Empty:
  inline def apply[T: Empty]: Empty[T] = summon[Empty[T]]

  given [T >: Null <: AnyRef]: Empty[T] with
    override def isEmpty(value: T): Boolean = value eq null
    override def nonEmpty(value: T): Boolean = value ne null
    override def empty: T = null.asInstanceOf[T]
  end given

end Empty

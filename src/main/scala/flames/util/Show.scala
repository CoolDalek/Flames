package flames.util

import scala.deriving.*
import scala.compiletime.*
import scala.reflect.*
import scala.util.NotGiven

@FunctionalInterface
trait Show[T] {

  extension (self: T) {

    def show: String

  }

}
object Show extends Summoner[Show] {
  
  private val unsafeInstance: Show[Any] = _.toString

  def unsafeShow[T]: Show[T] = unsafeInstance.asInstanceOf[Show[T]]

  given toStringShow[T <: Product]: Show[T] = unsafeShow

  inline def summonInstances[T <: Tuple]: List[Show[_]] =
    inline erasedValue[T] match
      case _: EmptyTuple => Nil
      case _: (t *: ts) => summonInline[Show[t]] :: summonInstances[ts]


  inline given derived[T](using mirror: Mirror.Of[T]): Show[T] = {
    val elements = summonInstances[mirror.MirroredElemTypes]
    inline mirror match {
      case sum: Mirror.SumOf[T] =>
        (obj: T) => {
          val index = sum.ordinal(obj)
          elements(index).asInstanceOf[Show[T]].show(obj)
        }
      case _: Mirror.ProductOf[T] =>
        inline val name = valueOf[mirror.MirroredLabel]
        (obj: T) => {
          val all = obj.asInstanceOf[Product]
            .productIterator
            .zip(elements.iterator)
          iterable(name, all) { (elem, show) =>
            show.asInstanceOf[Show[Any]].show(elem)
          }
        }
    }
  }

  inline private[util] def iterable[T, R <: IterableOnce[T]](name: String, instance: R)
                                                            (inline show: T => String): String = {
    val builder = new StringBuilder(name)
    builder += '('
    val iterator = instance.iterator
    if(iterator.hasNext) {
      builder ++= show(iterator.next)
      while(iterator.hasNext) {
        builder += ','
        builder ++= show(iterator.next)
      }
    }
    builder += ')'
    builder.result
  }

  inline given erasedIterable[T: Show, R <: Iterable[T]](using NotGiven[ClassTag[R]]): Show[R] =
    (obj: R) => {
      val name = "Erased iterable"
      Show.iterable(name, obj)(_.show)
    }

}
inline given Show[String] = identity
inline given Show[Byte] = Show.unsafeShow
inline given Show[Short] = Show.unsafeShow
inline given Show[Int] = Show.unsafeShow
inline given Show[Long] = Show.unsafeShow
inline given Show[Float] = Show.unsafeShow
inline given Show[Double] = Show.unsafeShow
inline given Show[Boolean] = Show.unsafeShow
inline given Show[Char] = Show.unsafeShow
inline given [T <: AnyVal]: Show[T] = Show.unsafeShow
inline given [T: Show, R <: Iterable[T]: ClassTag]: Show[R] =
  (obj: R) => {
    val name = classTag[R].runtimeClass.getSimpleName
    Show.iterable(name, obj)(_.show)
  }
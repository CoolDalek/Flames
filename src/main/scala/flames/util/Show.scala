package flames.util

import scala.deriving.*
import scala.compiletime.*
import scala.reflect.*

@FunctionalInterface
trait Show[T] {

  def show(obj: T): String

}
object Show extends Summoner[Show] {
  
  private val unsafeInstance: Show[Any] = _.toString

  def unsafeShow[T]: Show[T] = unsafeInstance.asInstanceOf[Show[T]]

  given [T <: Product]: Show[T] = unsafeShow

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
      case product: Mirror.ProductOf[T] =>
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

}
given Show[String] = identity
given [T <: AnyVal]: Show[T] = Show.unsafeShow
given [T: Show, R <: Iterable[T]](using ClassTag[R]): Show[R] =
  (obj: R) => {
    val name = classTag[R].runtimeClass.getSimpleName
    Show.iterable(name, obj) { elem =>
      elem.show
    }
  }
extension [T: Show](obj: T) {

  inline def show: String = Show[T].show(obj)

}
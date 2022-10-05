package flames.concurrent.execution.handles

import Field.*

trait Field[Self, Of]:
  outer =>

  type Handle[x] <: VarHandle.Tag[x]
  def handle: Handle[Self]

  def of: Class[Of]
  def self: Class[Self]

  def name: String

  def isStatic: Boolean
  def isMutable: Boolean

  import scala.reflect.*
  import scala.compiletime.*

  /**
   * We always can guarantee presence of field in subclasses,
   * but base field type can be incompatible with subclass type in some cases
   * */
  inline def narrow[T <: Self: ClassTag, R <: Of: ClassTag]: Option[Field[T, R]] =
    find[T, R](name).flatMap { subtype =>
      if (isMutable && subtype) None
      else Some(
        new Field[T, R] {

          override type Handle[x] = outer.Handle[x]
          override val handle: Handle[T] = outer.handle.asInstanceOf[Handle[T]]

          override val of: Class[R] = classTag[R].runtimeClass.asInstanceOf[Class[R]]
          override val self: Class[T] = classTag[T].runtimeClass.asInstanceOf[Class[T]]

          override val name: String = outer.name

          override val isStatic: Boolean = outer.isStatic
          override val isMutable: Boolean = outer.isMutable

        }
      )
    }
  end narrow

object Field:

  opaque type Scoped[T] = Any
  inline def inScope[T, R](container: R)(using Field[T, R]): Scoped[T] = container

  opaque type Magnet[T, R] <: R = R
  inline def magnetize[T, R](container: R)(using Field[T, R]): Magnet[T, R] = container

  type Aux[Self, Of, RefinedHandle[x] <: VarHandle.Tag[x]] = Field[Self, Of] {
    type Handle[x] = RefinedHandle[x]
  }

  extension [T, R, C[x] <: VarHandle.Tag[x]](self: Aux[T, R, C]) {

    inline def eraseHandle: Field[T, R] = self.asInstanceOf[Field[T, R]]

  }
  /**
   * None -> there is no such field
   * Some(true) -> there is a field with type T <:< Self
   * Some(false) -> there is a field with type Self
   * */
  inline def find[Self, Of](name: String): Option[Boolean] =
    Macro.findField[Self, Of](name)

end Field

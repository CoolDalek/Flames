package flames.actors.path

import flames.actors.utils.*

import java.util.Objects
import scala.reflect.*

class ActorSelector private(
                             private[actors] val name: String,
                             private[actors] val unique: Unique | Null,
                           ) {

  def nameCondition: String =
    name

  def uniqueCondition: Option[Unique] =
    unique.toOption

  override def hashCode(): Int = Objects.hash(name, unique)

  override def equals(obj: Any): Boolean =
    obj match
      case that: ActorSelector =>
        name == that.name && (unique eq that.unique)
      case _ => false
  end equals

  override def toString: String =
    val namePart = s"Actor selector by: name=$name"
    val uniquePart = unique.mapOrElse(
      x => s", unique=$x",
      "",
    )
    s"$namePart$uniquePart"
  end toString

}
object ActorSelector {

  extension (self: ActorSelector) {

    def /(other: ActorSelector): Vector[ActorSelector] = Vector(self, other)

    def /(other: Vector[ActorSelector]): Vector[ActorSelector] = other.prepended(self)

  }

  extension (self: Vector[ActorSelector]) {

    def /(other: ActorSelector): Vector[ActorSelector] = self.appended(other)

    def /(other: Vector[ActorSelector]): Vector[ActorSelector] = self.appendedAll(other)

  }

  def apply(name: String, unique: Unique | Null = null): ActorSelector =
    new ActorSelector(name, unique)

}
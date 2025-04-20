package flames.actors.path

import flames.actors.utils.*

import java.util.Objects
import scala.reflect.*

case class ActorSelector(
  name: String,
  unique: Unique | Null = null,
) {

  def matches(path: ActorPath): Boolean =
    path.name == name && unique.mapOrElse(
      x => x == path.unique,
      true,
    )

  def /(that: ActorSelector): Vector[ActorSelector] = Vector(this, that)

  def /(that: Vector[ActorSelector]): Vector[ActorSelector] = that.prepended(this)

}
object ActorSelector {

  extension (self: Vector[ActorSelector]) {

    def /(other: ActorSelector): Vector[ActorSelector] = self.appended(other)

    def /(other: Vector[ActorSelector]): Vector[ActorSelector] = self.appendedAll(other)

  }

}
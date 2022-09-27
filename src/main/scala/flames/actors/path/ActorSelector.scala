package flames.actors.path

import flames.actors.utils.*

import java.util.Objects
import scala.reflect.*

sealed trait ActorSelector {

  private[actors] def name: String
  private[actors] def unique: Unique | Null

  def nameCondition: String =
    name

  def uniqueCondition: Option[Unique] =
    unique.toOption

  def matches(path: ActorPath): Boolean

  protected inline def matchUnique(path: ActorPath): Boolean =
    unique.mapOrElse(
      x => x == path.unique,
      true
    )

}
object ActorSelector {

  private[actors] case class Simple(
                                     name: String, 
                                     unique: Unique | Null,
                                   ) extends ActorSelector {
    def matches(path: ActorPath): Boolean =
      path.name == name && matchUnique(path)
    
  }

  private[actors] case class Remote(
                                     name: String, 
                                     unique: Unique | Null, 
                                     host: String, 
                                     port: Int,
                                   ) extends ActorSelector {
    override def matches(path: ActorPath): Boolean =
      path match
        case remote: ActorPath.Remote =>
          remote.name == name && remote.host == host && remote.port == port && matchUnique(remote)
        case _ => false
    end matches
  }

  extension (self: ActorSelector) {

    def /(other: ActorSelector): Vector[ActorSelector] = Vector(self, other)

    def /(other: Vector[ActorSelector]): Vector[ActorSelector] = other.prepended(self)

  }

  extension (self: Vector[ActorSelector]) {

    def /(other: ActorSelector): Vector[ActorSelector] = self.appended(other)

    def /(other: Vector[ActorSelector]): Vector[ActorSelector] = self.appendedAll(other)

  }

  def apply(name: String, unique: Unique | Null = null): ActorSelector =
    Simple(name, unique)

  def remote(name: String, host: String, port: Int, unique: Unique | Null = null): ActorSelector =
    Remote(name, unique, host, port)

}
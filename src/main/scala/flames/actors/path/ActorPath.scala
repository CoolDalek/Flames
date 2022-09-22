package flames.actors.path

import java.util.Objects

import ActorPath.*

sealed trait ActorPath {
  
  def unique: Unique
  
  def name: String

  def parent: ActorPath

  inline def value: String = toString

  override def hashCode(): Int = Objects.hash(name, unique)

}
object ActorPath {

  private class Child(
                       val parent: ActorPath,
                       val name: String,
                       val unique: Unique,
                     ) extends ActorPath {
    override def toString: String = s"$parent/$name-$unique"

    override def equals(obj: Any): Boolean =
      obj match
        case that: Child =>
          this.name == that.name && this.unique == that.unique && this.parent == that.parent
        case _ => false
    end equals

  }
  private class Root(
                      val name: String,
                      val unique: Unique,
                    ) extends ActorPath {
    override def parent: ActorPath = this
    override def toString: String = name

    override def equals(obj: Any): Boolean =
      obj match
        case that: Root =>
          this.name == that.name && this.parent == that.parent
        case _ => false
    end equals
    
  }

  private[actors] def child(parent: ActorPath, name: String): ActorPath =
    Child(
      parent,
      name,
      parent.unique.next(),
    )

  private[actors] def root(system: String, unique: Unique): ActorPath =
    Root(
      name = system,
      unique = unique,
    )

}
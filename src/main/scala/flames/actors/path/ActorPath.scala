package flames.actors.path

import scala.annotation.threadUnsafe

sealed trait ActorPath {
  
  def unique: Unique
  
  def name: String

  def parent: ActorPath

  def value: String

  final override def toString: String = value

}
object ActorPath {

  private case class Child(
                            parent: ActorPath,
                            name: String,
                            unique: Unique,
                          ) extends ActorPath {
    @threadUnsafe lazy val value: String = s"$parent/$name-$unique"
  }

  private case class Root(
                           name: String,
                           unique: Unique,
                         ) extends ActorPath {
    override def parent: ActorPath = this
    override def value: String = name
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
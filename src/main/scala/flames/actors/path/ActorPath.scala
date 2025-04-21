package flames.actors.path

import flames.actors.path.ActorPath.*

import java.util.Objects

sealed trait ActorPath {

  def unique: Unique

  def name: String

  def parent: ActorPath

}
private[actors] object ActorPath {

  case class Child(
    parent: ActorPath,
    name: String,
    unique: Unique,
  ) extends ActorPath {
    override def toString: String = s"$parent/$name-$unique"
  }

  case class Local(
    name: String,
    unique: Unique,
  ) extends ActorPath {
    override def parent: ActorPath = this

    override def toString: String = s"$name-$unique"
  }

  case class Remote(
    name: String,
    unique: Unique,
    host: String,
    port: Int,
  ) extends ActorPath {
    override def parent: ActorPath = this

    override def toString: String = s"$host:$port//$name-$unique"
  }

  def child(parent: ActorPath, name: String): ActorPath =
    Child(
      parent,
      name,
      parent.unique.next(),
    )

  def local(system: String, unique: Unique): ActorPath =
    Local(
      name = system,
      unique = unique,
    )

  def remote(system: String, unique: Unique, host: String, port: Int): ActorPath =
    Remote(
      name = system,
      unique = unique,
      host = host,
      port = port,
    )

}

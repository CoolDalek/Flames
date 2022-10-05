package flames.concurrent.actors.path

import java.util.Objects

import ActorPath.*

sealed trait ActorPath {

  def unique: Unique

  def name: String

  def parent: ActorPath

  inline def value: String = toString

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
    override def toString: String = name

  }

  case class Remote(
                     name: String,
                     unique: Unique,
                     host: String,
                     port: Int,
                   ) extends ActorPath {

    override def parent: ActorPath = this

    override def toString: String = name

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

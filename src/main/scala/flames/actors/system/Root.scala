package flames.actors.system

import flames.actors.behavior.Behavior
import flames.actors.system.Root.Protocol
import flames.actors.{Actor, ActorEnv, StateAccess}

import scala.reflect.ClassTag

class Root(name: String)(using ActorEnv[Protocol]) extends Actor[Protocol](name) {

  override protected def act(): Behavior[Protocol] =
    receive {
      case cmd: Root.Spawn[?, ?] =>
        val erased = cmd.erase
        val child = spawnObj(erased.factory)(using erased.tag, summon[StateAccess])
        erased.complete(child)
        same
    }.ignoreSystem

}
object Root {

  sealed trait Protocol

  private[actors] class Spawn[A, B <: Actor[A]](
    val factory: ActorEnv[A] ?=> B,
    val complete: B => Unit,
    val tag: ClassTag[A],
  ) extends Protocol {

    def erase: Spawn[Any, Actor[Any]] =
      this.asInstanceOf[Spawn[Any, Actor[Any]]]

  }

}
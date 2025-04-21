package flames.actors.system

import flames.actors.behavior.Behavior
import flames.actors.system.Root.*
import flames.actors.{Actor, ActorEnv, ActorRef, StateAccess}

import scala.reflect.ClassTag

class Root(name: String)(using ActorEnv[Protocol]) extends Actor[Protocol](name):

  override protected def act(): Behavior[Protocol] =
    receive {
      case cmd: Root.Spawn[?, ?] =>
        val erased = cmd.erase
        val child = spawnObj(erased.factory)(using erased.tag, summon[StateAccess])
        erased.complete(child)
        same
      case cmd: Register[?] =>
        selfRef.addChild(cmd.ref)
        cmd.done()
        same
    }.ignoreSystem

object Root:

  sealed trait Protocol

  private[actors] class Spawn[A, B <: Actor[A]](
    val factory: ActorEnv[A] ?=> B,
    val complete: B => Unit,
    val tag: ClassTag[A],
  ) extends Protocol {

    inline def erase: Spawn[Any, Actor[Any]] =
      this.asInstanceOf[Spawn[Any, Actor[Any]]]

  }

  private[actors] class Register[T](
    val ref: ActorRef[T],
    val done: () => Unit,
  ) extends Protocol

end Root

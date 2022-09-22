package flames.actors.system

import flames.actors.ActorEnv
import flames.actors.behavior.Behavior
import flames.actors.message.DeliveryFailure
import flames.actors.system.Root.Protocol
import flames.actors.{Actor, StateAccess}

import scala.reflect.{ClassTag, classTag}

class Root(name: String)(using ActorEnv[Protocol]) extends Actor[Protocol](name) {

  override protected def act(): Behavior[Protocol] =
    receive {
      case cmd: Spawn[?, ?] =>
        cmd.perform()
        same
    }.ignore

  def makeSpawn[T: ClassTag, R <: Actor[T]](
                                             factory: ActorEnv[T] ?=> R,
                                             complete: DeliveryFailure | R => Unit
                                           ): Spawn[T, R] =
    Spawn[T, R](
      factory,
      complete,
      classTag[T],
    )
  end makeSpawn

  class Spawn[T, R <: Actor[T]](
                                 factory: ActorEnv[T] ?=> R,
                                 complete: R => Unit,
                                 tag: ClassTag[T],
                               ) extends Protocol {

    def perform()(using StateAccess): Unit = {
      given ClassTag[T] = tag
      val result = spawnObj[T, R](factory)
      complete(result)
    }

  }

}

object Root {

  sealed trait Protocol

}
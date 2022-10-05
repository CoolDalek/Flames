package flames.concurrent.actors.system

import flames.concurrent.actors.ActorEnv
import flames.concurrent.actors.behavior.Behavior
import flames.concurrent.actors.message.DeliveryFailure
import flames.concurrent.actors.system.Root.Protocol
import flames.concurrent.actors.{Actor, StateAccess}

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
package flames.concurrent.actors.pattern

import flames.concurrent.actors.Actor
import flames.concurrent.actors.ActorEnv
import flames.concurrent.actors.message.*

class Asker[T](
                complete: DeliveryFailure | T => Unit,
                timeout: Timeout,
              )(using ActorEnv[Any]) extends Actor[Any]("asker") {
  
  override def act(): Behavior[Any] =
    scheduleToSelf(timeout.asDuration, DeliveryFailure.TimedOut)
    receive { msg =>
      complete.asInstanceOf[Any => Unit](msg)
      stop
    }.ignore
  end act
  
}
package flames.actors.pattern

import flames.actors.Actor
import flames.actors.ActorEnv
import flames.actors.message.*

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
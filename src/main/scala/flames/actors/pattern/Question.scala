package flames.actors.pattern

import flames.actors.message.*
import flames.actors.{Actor, ActorEnv}

class Question[T](
  complete: DeliveryFailure | T => Unit,
  timeout: Timeout,
)(using ActorEnv[Any]) extends Actor[Any]("question") {

  override def act(): Behavior[Any] =
    scheduleToSelf(timeout.asDuration, DeliveryFailure.TimedOut)
    receive { msg =>
      complete.asInstanceOf[Any => Unit](msg)
      stop
    }.ignoreSystem
  end act

}
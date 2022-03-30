package flames.concurrent

import scala.concurrent.duration.FiniteDuration

trait ActorScheduler extends Scheduler {
  
  def scheduleMessage[T](delay: FiniteDuration, to: ActorRef[T], message: T): Cancellable
  
  def scheduleMessage[T](delay: FiniteDuration, period: FiniteDuration, to: ActorRef[T], message: T): Cancellable

}
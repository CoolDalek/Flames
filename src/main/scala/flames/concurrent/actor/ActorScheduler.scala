package flames.concurrent.actor

import flames.concurrent.execution.*
import flames.logging.FailureReporter

import scala.concurrent.duration.FiniteDuration

trait ActorScheduler extends Scheduler with FailureReporter {
  
  def scheduleMessage[T](delay: FiniteDuration, to: ActorRef[T], message: T): Cancellable
  
  def scheduleMessage[T](delay: FiniteDuration, period: FiniteDuration, to: ActorRef[T], message: T): Cancellable

  override def config: ActorsConfig

}
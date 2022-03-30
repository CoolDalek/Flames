package flames.concurrent

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.ExecutionContext
import flames.util.Logger

import scala.collection.concurrent.TrieMap
import scala.collection.mutable

case class ActorRuntime(
                         logger: Logger,
                         scheduler: Scheduler,
                         autoYieldCount: Int = 8,
                         autoYieldTime: Option[FiniteDuration] = None,
                       ) extends ActorScheduler {
  export scheduler.*

  private val refs = TrieMap.empty[ActorPath[Nothing], ActorRef[Nothing]]

  def spawn[T](name: String, factory: ActorFactory[T]): ActorRef[T] =
    spawn(name, ActorPath.Root, factory)

  private[concurrent] def spawn[T](name: String, parent: ActorPath[Nothing], factory: ActorFactory[T]): ActorRef[T] = {
    val actor = factory(name, parent, this)
    refs.addOne(actor.path, actor.self)
    actor.self
  }

  private[concurrent] def removeRef(path: ActorPath[Nothing]): Unit = refs.remove(path)

  def resolve[T](path: ActorPath[T]): Option[ActorRef[T]] =
    refs.get(path).map(_.asInstanceOf[ActorRef[T]])

  override def scheduleMessage[T](delay: FiniteDuration, to: ActorRef[T], message: T): Cancellable =
    schedule(delay)(to.timerTell(message))

  override def scheduleMessage[T](delay: FiniteDuration, period: FiniteDuration, to: ActorRef[T], message: T): Cancellable =
    schedule(delay, period)(to.timerTell(message))

}
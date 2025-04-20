package flames.actors.system

import flames.actors.message.DeliveryFailure
import flames.actors.path.ActorPath
import flames.actors.{Actor, ActorEnv, ActorRef, ActorSystem}

import java.util.UUID
import scala.util.control.NonFatal

trait DeadLetters {

  def publish[T](message: T, target: ActorPath, reason: DeliveryFailure): Unit

  def subscribe(handler: PartialFunction[DeadLetters.Event, Unit]): Cancellable

}
object DeadLetters {

  trait Event {

    def message: Any

    def target: ActorPath

    def reason: DeliveryFailure

  }

  type Factory = ActorSystem => DeadLetters

  case class Subscription(handler: PartialFunction[Event, Unit], cancel: Cancellable.Signal)

  enum Protocol {
    case Dead(message: Any, target: ActorPath, reason: DeliveryFailure) extends Protocol with Event
    case Subscribe(token: String, sub: Subscription)
    case Unsubscribe(token: String)
  }

  import Protocol.*

  class Default(using ActorEnv[Protocol]) extends DeadLetters with Actor[Protocol]("dead-letter"):
    override def publish[T](message: T, target: ActorPath, reason: DeliveryFailure): Unit =
      self.tell(
        Dead(message, target, reason),
      )

    override def subscribe(handler: PartialFunction[Event, Unit]): Cancellable =
      val token = UUID.randomUUID().toString
      val signal = Cancellable.signal {
        self.tell(
          Unsubscribe(token),
        )
      }
      self.tell(
        Subscribe(token, Subscription(handler, signal)),
      )
      signal
    end subscribe

    import scala.collection.mutable

    private val subscriptions = mutable.Map.empty[String, Subscription]

    override protected def act(): Behavior[Protocol] =
      receive {
        case event: Dead =>
          subscriptions.foreach { (_, sub) =>
            try {
              sub.handler(event)
            } catch case NonFatal(_) => () // Don't throw exceptions in your subscriptions
            //TODO: LOGGING
          }
          same
        case Subscribe(token, sub) =>
          subscriptions.update(token, sub)
          same
        case Unsubscribe(token) =>
          subscriptions.remove(token)
            .foreach(_.cancel.cancelled())
          same
      }.ignoreSystem

  end Default

}
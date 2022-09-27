package flames.actors.system

import flames.actors.ActorEnv.ActorEnv
import flames.actors.{Actor, ActorRef, ActorSystem}
import flames.actors.message.DeliveryFailure
import flames.actors.path.ActorPath

import java.util.UUID
import scala.util.control.NonFatal

trait DeadLetter {
  
  def publish[T](message: T, target: ActorPath, reason: DeliveryFailure): Unit

  def subscribe(handler: PartialFunction[DeadLetterEvent, Unit]): Cancellable

}
object DeadLetter {

  type Factory = ActorSystem => DeadLetter


  case class Subscription(handler: PartialFunction[DeadLetterEvent, Unit], cancel: Cancellable.Signal)
  enum Protocol {
    case Dead(message: Any, target: ActorPath, reason: DeliveryFailure) extends Protocol with DeadLetterEvent
    case Subscribe(token: String, sub: Subscription)
    case Unsubscribe(token: String)
  }

  import Protocol.*
  class Default(using ActorEnv[Protocol]) extends DeadLetter with Actor[Protocol]("dead-letter"):
    override def publish[T](message: T, target: ActorPath, reason: DeliveryFailure): Unit =
      self.tell(
        Dead(message, target, reason)
      )

    override def subscribe(handler: PartialFunction[DeadLetterEvent, Unit]): Cancellable =
      val token = UUID.randomUUID().toString
      val signal = Cancellable.signal {
        self.tell(
          Unsubscribe(token)
        )
      }
      self.tell(
        Subscribe(token, Subscription(handler, signal))
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
      }.ignore

  end Default

}
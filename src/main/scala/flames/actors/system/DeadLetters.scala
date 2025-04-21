package flames.actors.system

import flames.actors.message.DeliveryFailure
import flames.actors.path.{ActorPath, Unique}
import flames.actors.{Actor, ActorEnv, ActorRef, ActorSystem}

import java.util.UUID
import scala.util.control.NonFatal

trait DeadLetters:

  def publish[T](message: T, target: ActorPath, reason: DeliveryFailure): Unit

  def subscribe(handler: DeadLetters.Event => Unit): Cancellable

object DeadLetters:

  trait Event {

    def message: Any

    def target: ActorPath

    def reason: DeliveryFailure

  }

  case class Subscription(
    handler: Event => Unit,
    cancel: Cancellable.Signal,
  )

  enum Protocol {
    case DeadMessage(
      message: Any,
      target: ActorPath,
      reason: DeliveryFailure,
    ) extends Protocol with Event
    case Subscribe(token: Unique, sub: Subscription)
    case Unsubscribe(token: Unique)
  }

  def default(
    unique: Unique = Unique.reference(),
  )(using ActorEnv[Protocol]): (DeadLetters, ActorRef[Protocol]) =
    val impl = new DeadLetters with Actor[Protocol]("dead-letters"):
      import Protocol.*

      override def publish[T](message: T, target: ActorPath, reason: DeliveryFailure): Unit =
        self.tell(
          DeadMessage(message, target, reason),
        )

      override def subscribe(handler: Event => Unit): Cancellable =
        val token = unique.next()
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

      private val subscriptions = mutable.Map.empty[Unique, Subscription]

      override protected def act(): Behavior[Protocol] =
        receive {
          case event: DeadMessage =>
            subscriptions.foreach { (_, sub) =>
              try sub.handler(event)
              catch case NonFatal(exc) =>
                system.reportFailure(exc)
            }
            same
          case Subscribe(token, sub) =>
            subscriptions.update(token, sub)
            same
          case Unsubscribe(token) =>
            subscriptions
              .remove(token)
              .foreach(_.cancel.cancelled())
            same
        }.ignoreSystem
    end impl
    (impl, impl.self)
  end default

end DeadLetters

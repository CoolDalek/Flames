package flames.actors

import flames.actors.message.*
import SystemMessage.InternalMessage
import flames.actors.path.ActorPath
import flames.actors.pattern.Wait

type ErasedRef = ActorRef[Nothing]
trait ActorRef[-T] {

  def tell(msg: T): Unit

  private[actors] def timerTell(msg: T): Unit

  private[actors] def internalTell(msg: InternalMessage): Unit

  def ackTell[F[_] : Wait](msg: T): F[Ack[Unit]]

  def ask[F[_] : Wait, Response](request: ActorRef[Response] => T)(using Timeout): F[Ack[Response]]
  
  private[actors] def watchRequest[R](ref: ActorRef[R]): Unit
  
  private[actors] def unwatchRequest[R](ref: ActorRef[R]): Unit

  def path: ActorPath

  def tag: Class[?]

  override def equals(obj: Any): Boolean =
    obj match
      case that: ActorRef[?] =>
        path == that.path
      case _ => false
  end equals

  override def hashCode(): Int = path.##

}

package flames.concurrent.actor

import flames.concurrent.actor.mailbox.SystemMessage

trait ActorRef[-T] {

  def tell(message: T): Unit
  
  private[concurrent] def timerTell(message: T): Unit
  
  private[concurrent] def systemTell(message: SystemMessage): Unit
  
  def stop(): Unit
  
  private[concurrent] def silentStop(): Unit

  def path: ActorPath[T]

}
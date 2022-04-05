package flames.concurrent.actor

trait ActorRef[-T] {

  def tell(message: T): Unit
  
  private[concurrent] def timerTell(message: T): Unit
  
  def stop(): Unit

}
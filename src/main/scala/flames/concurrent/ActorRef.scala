package flames.concurrent

trait ActorRef[-T] {

  def tell(message: T): Unit
  
  private[concurrent] def timerTell(message: T): Unit

  def path: ActorPath[T]

  def name: String
  
  def stop(): Unit

}
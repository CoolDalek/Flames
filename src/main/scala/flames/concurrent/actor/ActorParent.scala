package flames.concurrent.actor

type ActorParent = ActorRef[Nothing] | Null
object ActorParent {
  
  inline def root: ActorParent = null

}
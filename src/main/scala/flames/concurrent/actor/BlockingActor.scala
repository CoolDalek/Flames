package flames.concurrent.actor

trait BlockingActor[T](using ActorRuntime) extends AnyActor[T] {
  
  final override protected def makeFiber: ActorFiber[T] = BlockingFiber(runtime, act())
  
}
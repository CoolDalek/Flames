package flames.concurrent.actor

trait Actor[T](using ActorRuntime) extends AnyActor[T] {
  
  final override protected def makeFiber: ActorFiber[T] = AsyncFiber(runtime, act())
  
}
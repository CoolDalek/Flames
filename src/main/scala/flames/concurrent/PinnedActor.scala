package flames.concurrent

trait PinnedActor[T](using ActorRuntime) extends Actor[T] {
  
  final override protected[concurrent] val fiber: ActorFiber[T] =
    PinnedFiber(runtime, act())
  
}
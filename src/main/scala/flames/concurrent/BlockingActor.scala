package flames.concurrent

trait BlockingActor[T](using ActorRuntime) extends Actor[T] {
  
  final override protected[concurrent] lazy val fiber: ActorFiber[T] =
    BlockingFiber(runtime, act())
  
}
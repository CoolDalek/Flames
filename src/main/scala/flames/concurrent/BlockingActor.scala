package flames.concurrent

import flames.concurrent.ActorFiber.*

trait BlockingActor[T](using ActorRuntime) extends Actor[T] {
  
  override protected[concurrent] val fiber: ActorFiber[T] =
    BlockingFiber(runtime, act())
  
}
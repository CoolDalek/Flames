package flames.concurrent.actor

trait PinnedActor[T](customThread: PinnedFiber.CustomThread = null)(using ActorRuntime) extends AnyActor[T] {
  
  final override protected def makeFiber: ActorFiber[T] =
    PinnedFiber(
      runtime,
      act(),
      customThread,
    ).initialize()
  
}
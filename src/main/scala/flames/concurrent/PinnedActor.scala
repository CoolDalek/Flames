package flames.concurrent

trait PinnedActor[T](customThread: PinnedFiber.CustomThread = null)(using ActorRuntime) extends Actor[T] {

  override protected[concurrent] lazy val fiber: ActorFiber[T] = {
    val pinned = PinnedFiber(
      runtime,
      act(),
      customThread,
    )
    pinned.initialize()
    pinned
  }
  
}
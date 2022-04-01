package flames.concurrent

trait PinnedActorThreadFactory {

  def makeThread(pool: PinnedActorThreadPool): PinnedActorThread

}
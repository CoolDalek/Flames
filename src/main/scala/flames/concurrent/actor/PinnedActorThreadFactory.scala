package flames.concurrent.actor

trait PinnedActorThreadFactory {

  def makeThread(pool: PinnedActorThreadPool): PinnedActorThread

}
package flames.concurrent.actor

import java.util.UUID
import flames.util.Show

import java.util.concurrent.atomic.AtomicLong

opaque type ActorPath[-T] = String
object ActorPath {

  final class Factory(uniquenessProvider: () => String) {

    def apply[T](name: String, parent: ActorParent): ActorPath[T] = {
      val prefix = if(parent eq null) "root" else parent.asInstanceOf[ActorRef[Nothing]].path
      val escaped = name.replaceAll("/", "_slash_")
      val postfix = uniquenessProvider()
      s"$prefix/$escaped-$postfix"
    }

  }

  def countedFactory: Factory = {
    val counter = new AtomicLong(Long.MinValue)
    new Factory(counter.incrementAndGet().toString)
  }

  def uuidFactory: Factory =
    new Factory(UUID.randomUUID().toString)

  def timestampFactory: Factory =
    new Factory(System.nanoTime().toString)

  inline def defaultFactory: Factory = uuidFactory

  inline given [T]: Show[ActorPath[T]] = identity

}
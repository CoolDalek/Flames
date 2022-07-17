package flames.concurrent.actor

import java.util.UUID
import flames.util.Show

import java.util.concurrent.atomic.AtomicLong

opaque type ActorToken = String
object ActorToken {
  
  extension (self: ActorToken) {
    
    inline private[actor] def parts: Array[String] = self.split("/")
    
  }
  
  extension (self: String) {
    
    inline private[actor] def concatToken(other: String): ActorToken = s"$self/$other"
    
  }

  final class Factory(uniquenessProvider: () => String) {

    def apply(name: String, parent: ActorParent): ActorToken = {
      val prefix = if(parent eq null) "root" else parent.asInstanceOf[ActorRef[Nothing]].token
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

  inline def default: Factory = uuidFactory

  inline given Show[ActorToken] = Show.unsafe.instance
  
}
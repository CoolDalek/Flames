package flames.concurrent.actor

import java.util.UUID

opaque type ActorToken = String
object ActorToken {
  inline def apply(): ActorToken = UUID.randomUUID().toString
}
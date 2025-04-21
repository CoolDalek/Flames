package flames.actors

import flames.actors.ActorSystem

import scala.reflect.ClassTag

type Parent = ErasedRef | Null

case class ActorEnv[T] private[actors](
  parent: Parent,
  system: ActorSystem,
  tag: ClassTag[T],
)
object ActorEnv:

  private[actors] inline def root[T](using
    tag: ClassTag[T],
    system: ActorSystem,
  ): ActorEnv[T] = ActorEnv(null, system, tag)

  private[actors] inline def make[T](parent: ErasedRef)(using
    tag: ClassTag[T],
    system: ActorSystem,
  ): ActorEnv[T] = ActorEnv(parent, system, tag)

end ActorEnv

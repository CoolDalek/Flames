package flames.actors

import flames.actors.ActorSystem

import scala.reflect.ClassTag

private[actors] type Parent = ErasedRef | Null
object ActorEnv {
  opaque type ActorEnv[T] = (Parent, ActorSystem, ClassTag[T])

  private[actors] inline def parent[T](using env: ActorEnv[T]): Parent = env._1

  private[actors] inline def system[T](using env: ActorEnv[T]): ActorSystem = env._2

  private[actors] inline def tag[T](using env: ActorEnv[T]): ClassTag[T] = env._3

  private[actors] inline def root[T](using tag: ClassTag[T], system: ActorSystem): ActorEnv[T] =
    (null, system, tag)

  private[actors] inline def make[T](parent: ErasedRef)
                                    (using tag: ClassTag[T], system: ActorSystem): ActorEnv[T] =
    (parent, system, tag)
}

export ActorEnv.ActorEnv

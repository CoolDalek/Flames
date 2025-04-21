package flames.actors.system

import flames.actors.ActorEnv
import flames.actors.behavior.Behavior
import flames.actors.fiber.{Children, Fiber}
import flames.actors.path.{ActorPath, Unique}
import flames.actors.ref.LocalRef
import flames.actors.utils.Nulls.*

import scala.reflect.{ClassTag, classTag}

trait RefProvider:

  def local[T](
    name: String,
    behavior: Behavior[T],
    config: ActorConfig,
  )(using env: ActorEnv[T]): LocalRef[T]

object RefProvider:

  def default(
    unique: Unique = Unique.increment(),
  ): RefProvider = new:

    override def local[T](
      name: String,
      behavior: Behavior[T],
      config: ActorConfig,
    )(using env: ActorEnv[T]): LocalRef[T] =
      val path = env.parent.mapOrElse(
        x => ActorPath.child(x.path, name),
        ActorPath.local(env.system.name, unique),
      )
      val fiber = Fiber(
        behavior,
        config.mkMailbox[T](),
        config.mkChildren(),
        config.autoYield,
        env.parent,
        env.system,
        path,
      )
      LocalRef[T](
        fiber,
        env.tag.runtimeClass,
      )
    end local

  end default

end RefProvider

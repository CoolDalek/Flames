package flames.actors.system

import flames.actors.pattern.Wait
import flames.actors.*
import flames.actors.message.Ack
import Ack.*

import scala.reflect.ClassTag

object Spawner {

  class Obj[F[_] : Wait](root: Root):

    inline def apply[T: ClassTag, R <: Actor[T]](inline actor: ActorEnv[T] ?=> R): F[R] =
      Wait[F].asyncAck[R] { callback =>
        root.selfRef.tell(
          root.makeSpawn(actor, callback)
        )
      }.map {
        case Delivered(value) => value
        case Undelivered(failure) => throw failure // If this happens we have really serious problems
      }
    end apply

  end Obj

  class Ref[F[_] : Wait](root: Root):

    inline def apply[T: ClassTag, R <: Actor[T]](inline actor: ActorEnv[T] ?=> R): F[ActorRef[T]] =
      Obj(root).apply(actor).map(_.selfRef)

  end Ref

  class All[F[_] : Wait](root: Root):

    inline def apply[T: ClassTag, R <: Actor[T]](inline actor: ActorEnv[T] ?=> R): F[(R, ActorRef[T])] =
      Obj(root).apply(actor).map(x => x -> x.selfRef)

  end All

  class Fire[F[_] : Wait](root: Root):

    inline def apply[T: ClassTag, R <: Actor[T]](inline actor: ActorEnv[T] ?=> R): F[Unit] =
      Obj(root).apply(actor).map(_ => ())

  end Fire

}

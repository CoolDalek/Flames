package flames.concurrent.actor

import flames.concurrent.execution.ExecutionModel

trait Spawn {

  def spawn[M, E <: ExecutionModel, A <: Actor[M, E]](factory: ActorEnv ?=> A): (A, ActorRef[M])

  def spawnActor[M, E <: ExecutionModel, A <: Actor[M, E]](factory: ActorEnv ?=> A): A =
    spawn(factory)._1

  def spawnRef[M, E <: ExecutionModel, A <: Actor[M, E]](factory: ActorEnv ?=> A): ActorRef[M] =
    spawn(factory)._2

}
object Spawn {

  trait WithChilds extends Spawn  {

    protected def env: ActorEnv

    protected def registerChild[T](path: ActorPath[T], actor: ActorRef[T]): Unit

    final override def spawn[M, E <: ExecutionModel, A <: Actor[M, E]](factory: ActorEnv ?=> A): (A, ActorRef[M]) = {
      val actor = spawnActor(factory)
      actor -> actor.self
    }

    final override def spawnActor[M, E <: ExecutionModel, A <: Actor[M, E]](factory: ActorEnv ?=> A): A = {
      val actor = factory(using env)
      actor.run()
      val ref = actor.self
      registerChild(ref.path, ref)
      actor
    }

    final override def spawnRef[M, E <: ExecutionModel, A <: Actor[M, E]](factory: ActorEnv ?=> A): ActorRef[M] = {
      spawnActor(factory).self
    }

  }

}
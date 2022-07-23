package flames.concurrent.actor

import flames.concurrent.execution.ExecutionModel

type ActorEnv = (ActorRuntime, ActorParent)
object ActorEnv {
  
  inline def apply()(using env: ActorEnv): ActorEnv = env
  
  inline def runtime(using env: ActorEnv): ActorRuntime = env._1
  
  inline def parent(using env: ActorEnv): ActorParent = env._2
  
  inline def withParent(parent: ActorParent)(using env: ActorEnv): ActorEnv = (runtime, parent)

}
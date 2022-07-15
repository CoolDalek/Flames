package flames.concurrent.actor

type ActorParent = ActorRef[Nothing] | Null
type ActorEnv = (ActorRuntime, ActorParent)
type ActorFactory[T] = ActorEnv ?=> Actor[T, ?]
object ActorEnv {
  
  inline def apply()(using env: ActorEnv): ActorEnv = env
  
  inline def runtime(using env: ActorEnv): ActorRuntime = env._1
  
  inline def parent(using env: ActorEnv): ActorParent = env._2
  
  inline def withParent(parent: ActorParent)(using env: ActorEnv): ActorEnv = (runtime, parent)

}
inline given (using runtime: ActorRuntime): ActorEnv = (runtime, null) 
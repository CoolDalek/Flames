package flames.concurrent.actors.behavior

type Receiver[-P, -R] = R => Behavior[P]
object Receiver {
  
  val Ignore: Receiver[Any, Any] = _ => Behavior.Same

}

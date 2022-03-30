package flames.concurrent

opaque type ActorPath[-T] = String
object ActorPath {
  val Root: ActorPath[Nothing] = "Root"
  inline def path[T](name: String): ActorPath[T] = name
}
extension [T](path: ActorPath[T]) {

  inline def /[R](child: String): ActorPath[R] = s"$path/$child"

}
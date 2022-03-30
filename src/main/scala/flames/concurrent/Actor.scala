package flames.concurrent

type ActorFactory[T] = (String, ActorPath[Nothing], ActorRuntime) => Actor[T]
trait Actor[T](
                name: String,
                parentPath: ActorPath[Nothing],
                runtime: ActorRuntime,
              ) { actor =>

  protected[concurrent] final val path: ActorPath[T] = parentPath / name

  private val fiber = ActorFiber(
    runtime,
    act(),
    path,
  )

  protected final def childs: Set[ActorRef[Nothing]] = fiber.getChilds

  protected[concurrent] final val self: ActorRef[T] = new ActorRef[T] {

    override def tell(message: T): Unit = userTell(message)

    override def path: ActorPath[T] = actor.path

    override def name: String = actor.name

    override def stop(): Unit = fiber.stop()

    override private[concurrent] def timerTell(message: T): Unit = actor.timerTell(message)

  }

  protected final def spawn[R](name: String, factory: ActorFactory[R]): ActorRef[R] = {
    val child = runtime.spawn(name, path, factory)
    fiber.addChild(child)
    child
  }

  private[concurrent] final def userTell(message: T): Unit =
    fiber.userTell(message)

  private[concurrent] final def timerTell(message: T): Unit =
    fiber.timerTell(message)

  def act(): Behavior[T]

}
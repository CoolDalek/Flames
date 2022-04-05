package flames.concurrent.actor

type ActorFactory[T] = ActorRuntime ?=> AnyActor[T]
trait AnyActor[T](using val runtime: ActorRuntime) {
  
  protected def makeFiber: ActorFiber[T]

  private var fiber: ActorFiber[T] = null.asInstanceOf[ActorFiber[T]]
  
  private[actor] def initialize(): ActorRef[T] = {
    fiber = makeFiber
    self
  }

  protected final val self: ActorRef[T] = new ActorRef[T] {

    override def tell(message: T): Unit = fiber.userTell(message)

    override def stop(): Unit = fiber.stop()

    override private[concurrent] def timerTell(message: T): Unit = fiber.timerTell(message)

  }

  sealed trait StateAccess

  protected final def childs(using StateAccess): Set[ActorRef[Nothing]] = fiber.getChilds

  protected final def spawn[R](factory: ActorFactory[R])(using StateAccess): ActorRef[R] = {
    val child = runtime.spawn(factory)
    fiber.addChild(child)
    child
  }

  inline protected def receive[T](inline act: StateAccess ?=> T => Behavior[T]): Behavior[T] = {
    given StateAccess = new StateAccess {}
    Behavior.Receive(act)
  }

  inline protected def same: Behavior[T] = Behavior.Same

  inline protected def stop: Behavior[T] = Behavior.Stop

  def act(): Behavior[T]

}
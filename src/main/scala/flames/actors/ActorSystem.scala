package flames.actors

import flames.actors.fiber.Children
import flames.actors.message.Mailbox
import flames.actors.path.*
import flames.actors.pattern.*
import flames.actors.remote.Client
import flames.actors.system.*
import flames.actors.utils.Logger

import java.util.concurrent.{Executors, ForkJoinPool}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import scala.reflect.{ClassTag, classTag}

trait ActorSystem extends ExecutionContext:
  protected given ActorSystem = this

  def name: String

  def config: ActorConfig

  def root: ActorRef[Root.Protocol]

  def path: ActorPath

  def deadLetters: DeadLetters

  def selector: Selector

  def refProvider: RefProvider

  def scheduler: Scheduler

  def blocker: ExecutionContext

  def spawn[F[_]: Wait, T: ClassTag, R <: Actor[T]](actor: ActorEnv[T] ?=> R): F[(R, ActorRef[T])] =
    spawnObj[F, T, R](actor).map(x => x -> x.self)

  def spawnForget[F[_]: Wait, T: ClassTag, R <: Actor[T]](actor: ActorEnv[T] ?=> R): F[Unit] =
    spawnObj[F, T, R](actor).void

  def spawnRef[F[_]: Wait, T: ClassTag, R <: Actor[T]](actor: ActorEnv[T] ?=> R): F[ActorRef[T]] =
    spawnObj[F, T, R](actor).map(_.self)

  def spawnObj[F[_]: Wait, T: ClassTag, R <: Actor[T]](actor: ActorEnv[T] ?=> R): F[R] =
    import flames.actors.message.Ack.*
    Wait[F].asyncAck[R] { callback =>
      root.tell(
        Root.Spawn(actor, callback, classTag[T]),
      )
    }.flatMap {
      case Delivered(value) => Wait[F].pure(value)
      // Shouldn't happen
      case Undelivered(failure) => Wait[F].raise(failure)
    }
  end spawnObj

  def scheduleMessage[T](
    delay: FiniteDuration,
    to: ActorRef[T],
    message: T,
  ): Cancellable

  def scheduleMessage[T](
    delay: FiniteDuration,
    period: FiniteDuration,
    to: ActorRef[T],
    message: T,
  ): Cancellable

object ActorSystem:

  val DefaultConfig: ActorConfig = ActorConfig(
    8,
    Mailbox.concurrentLinkedQueue,
    () => Children.sync,
  )

  def defaultBlocker(
    prefix: String,
    reporter: Logger.Fallback = Logger.DefaultFallback,
  ): ExecutionContext =
    ExecutionContext.fromExecutor(
      Executors.newCachedThreadPool(
        BlockingThreadFactory(
          s"$prefix-blocker",
          reporter,
        ),
      ),
      reporter,
    )

  def defaultCpu(
    prefix: String,
    reporter: Logger.Fallback = Logger.DefaultFallback,
    parallelism: Int = sys.runtime.availableProcessors(),
    maxBlocking: Int = -1,
  ): ExecutionContext =
    ExecutionContext.fromExecutor(
      ForkJoinPool(
        parallelism,
        FjpThreadFactory(
          s"$prefix-cpu",
          reporter,
          if maxBlocking > 0 then maxBlocking else parallelism,
        ),
        reporter,
        false,
      ),
      reporter,
    )

  def defaultScheduler(
    prefix: String,
    interruptRunning: Boolean = false,
    reporter: Logger.Fallback = Logger.DefaultFallback,
  ): Scheduler =
    Scheduler.java(
      s"$prefix-scheduler",
      interruptRunning,
      reporter,
    )

  private class DefaultSystem(override val name: String) extends ActorSystem {
    override val scheduler: Scheduler = defaultScheduler(name)
    override val blocker: ExecutionContext = defaultBlocker(name)
    private val cpu = defaultCpu(name)
    export cpu.*

    override def config: ActorConfig = DefaultConfig

    override val refProvider: RefProvider = RefProvider.default()

    override val root: ActorRef[Root.Protocol] =
      new Root(name)(using ActorEnv.root).self

    override def path: ActorPath = root.path

    val (deadQueue, deadRef) = DeadLetters.default()(using ActorEnv.make(root))

    override def deadLetters: DeadLetters = deadQueue
    override val selector: Selector = Selector(this, Client.noop)
    
    private class Timer[T](addr: ActorRef[T], envelope: T) extends Runnable:
      override def run(): Unit = addr.timerTell(envelope)


    override def scheduleMessage[T](
      delay: FiniteDuration,
      to: ActorRef[T],
      message: T,
    ): Cancellable =
      scheduler.delayed(delay)(Timer(to, message))

    override def scheduleMessage[T](
      delay: FiniteDuration,
      period: FiniteDuration,
      to: ActorRef[T],
      message: T,
    ): Cancellable =
      scheduler.withFixedDelay(delay, period)(Timer(to, message))

  }

  def default[F[_]: Wait](
    name: String,
  ): F[ActorSystem] =
    val impl = DefaultSystem(name)
    Wait[F].async[Unit] { cb =>
      impl.root.tell(
        Root.Register(
          impl.deadRef,
          () => cb(()),
        ),
      )
    }.as(impl)
  end default

end ActorSystem

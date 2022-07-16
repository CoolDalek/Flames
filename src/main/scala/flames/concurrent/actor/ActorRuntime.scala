package flames.concurrent.actor

import flames.concurrent.*
import flames.concurrent.actor.fiber.*
import flames.concurrent.actor.*
import flames.concurrent.actor.behavior.Behavior
import flames.logging.*
import flames.util.Id

import java.lang.Thread.UncaughtExceptionHandler
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger
import scala.collection.concurrent.TrieMap
import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

trait ActorRuntime extends ActorScheduler {

  protected given ActorRuntime = this

  def fiberConfig: FiberConfig

  def logger: Logger

  def spawn[Message, Type <: ActorType](actor: Actor[Message, Type]): ActorRef[Message] = {
    actor.fiber // trigger initialization
    actor.self
  }

  private[actor] def makeBlocking[T](behavior: Behavior[T], parent: ActorParent): ActorFiber[T]

  private[actor] def makePinned[T](behavior: Behavior[T], parent: ActorParent): ActorFiber[T]

  private[actor] def makeAsync[T](behavior: Behavior[T], parent: ActorParent): ActorFiber[T]

  override def scheduleMessage[T](delay: FiniteDuration, to: ActorRef[T], message: T): Cancellable =
    schedule(delay)(to.timerTell(message))

  override def scheduleMessage[T](delay: FiniteDuration, period: FiniteDuration, to: ActorRef[T], message: T): Cancellable =
    schedule(delay, period)(to.timerTell(message))

}
object ActorRuntime {

  // Same as [T] =>> ((Behavior[T], ActorParent, Scheduler, FiberConfig) => ActorFiber[T])
  // But for some reason lambda requires to set type parameter when passed
  trait FiberFactory {

    def apply[T](behavior:Behavior[T], parent: ActorParent, scheduler: Scheduler, config: FiberConfig): ActorFiber[T]

  }

  private class SimpleRuntime(
                               val logger: Logger,
                               val fiberConfig: FiberConfig,
                               val scheduler: Scheduler,
                               blockingFactory: FiberFactory,
                               pinnedFactory: FiberFactory,
                               asyncFactory: FiberFactory,
                             ) extends ActorRuntime {
    export scheduler.*

    override private[actor] def makeBlocking[T](behavior: Behavior[T], parent: ActorParent): ActorFiber[T] =
      blockingFactory[T](
        behavior,
        parent,
        scheduler,
        fiberConfig,
      )

    override private[actor] def makePinned[T](behavior: Behavior[T], parent: ActorParent): ActorFiber[T] =
      pinnedFactory[T](
        behavior,
        parent,
        scheduler,
        fiberConfig,
      )

    override private[actor] def makeAsync[T](behavior: Behavior[T], parent: ActorParent): ActorFiber[T] =
      asyncFactory[T](
        behavior,
        parent,
        scheduler,
        fiberConfig,
      )

  }

  def apply(
             logger: Logger,
             scheduler: Scheduler,
             blockingFactory: FiberFactory,
             pinnedFactory: FiberFactory,
             asyncFactory: FiberFactory,
             fiberConfig: FiberConfig = FiberConfig.default,
           ): ActorRuntime =
    new SimpleRuntime(
      logger = logger,
      fiberConfig = fiberConfig,
      scheduler = scheduler,
      blockingFactory = blockingFactory,
      pinnedFactory = pinnedFactory,
      asyncFactory = asyncFactory,
    )

  def default(
               logLevel: LogLevel,
               _fiberConfig: FiberConfig = FiberConfig.default,
               timestampPattern: String = ActorLogger.defaultTimestampPattern,
               printer: Printer[Id] = ActorLogger.defaultPrinter,
             ): ActorRuntime =
    new ActorRuntime {

      override val fiberConfig: FiberConfig = _fiberConfig

      inline private def makeFiber[T](
                                       inline executionFactory: FiberState[T] => ExecutionStrategy.Factory,
                                       behavior: Behavior[T],
                                       parent: ActorParent,
                                     ): ActorFiber[T] =
        val token = ActorToken()
        val state = FiberState.default[T](
          fiberConfig,
          parent,
          token,
        )
        val execution = executionFactory(state)
        ActorFiber[T](
          state,
          behavior,
          logger,
          execution,
        )
      end makeFiber

      inline private def makeShifted[T](
                                         ec: ExecutionContext,
                                         behavior: Behavior[T],
                                         parent: ActorParent,
                                       ): ActorFiber[T] =
        val fiber = makeFiber[T](
          state => ShiftedExecution[T](
            ec,
            state,
          ),
          behavior,
          parent,
        )
        fiber.run()
        fiber
      end makeShifted

      override def makeBlocking[T](behavior: Behavior[T], parent: ActorParent): ActorFiber[T] =
        makeShifted[T](blockingEC, behavior, parent)

      override def makeAsync[T](behavior: Behavior[T], parent: ActorParent): ActorFiber[T] =
        makeShifted[T](this, behavior, parent)

      override def makePinned[T](behavior: Behavior[T], parent: ActorParent): ActorFiber[T] =
        val fiber = makeFiber[T](
          PinnedExecution.apply[T],
          behavior,
          parent,
        )
        blockingEC.execute(fiber)
        fiber
      end makePinned

      val logger: Logger = RuntimeLogger(logLevel, timestampPattern, printer)

      val scheduler: Scheduler = Scheduler.default(logger)

      export scheduler.*

    }


}
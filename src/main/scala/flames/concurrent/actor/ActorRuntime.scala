package flames.concurrent.actor

import flames.concurrent.*
import flames.concurrent.actor.fiber.*
import flames.concurrent.actor.*
import flames.concurrent.actor.behavior.Behavior
import flames.concurrent.execution.*
import flames.logging.*
import flames.util.{Id, Show}
import sourcecode.{Enclosing, Line}

import java.lang.Thread.UncaughtExceptionHandler
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger
import scala.collection.concurrent.TrieMap
import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration
import scala.language.postfixOps

trait ActorRuntime extends ActorScheduler {

  protected given ActorRuntime = this

  def fiberConfig: FiberConfig

  def logger: Logger

  def rootChilds: Set[ActorRef[Nothing]]

  private[actor] def addRootChild[T](path: ActorPath[T], ref: ActorRef[Nothing]): Unit

  private[actor] def removeRootChild[T](path: ActorPath[T]): Option[ActorRef[Nothing]]

  private[actor] def getRootChild[T](path: ActorPath[T]): Option[ActorRef[Nothing]]

  def spawn[Message, Model <: ExecutionModel](actor: Actor[Message, Model]): ActorRef[Message] = {
    actor.fiber // trigger initialization
    val ref = actor.self
    addRootChild(ref.path, ref)
    ref
  }

  def pathFactory: ActorPath.Factory

  private[actor] def makeBlocking[T](name: String, behavior: Behavior[T], parent: ActorParent): ActorFiber[T]

  private[actor] def makePinned[T](name: String, behavior: Behavior[T], parent: ActorParent): ActorFiber[T]

  private[actor] def makeAsync[T](name: String, behavior: Behavior[T], parent: ActorParent): ActorFiber[T]

  override def scheduleMessage[T](delay: FiniteDuration, to: ActorRef[T], message: T): Cancellable =
    schedule(delay)(to.timerTell(message))

  override def scheduleMessage[T](delay: FiniteDuration, period: FiniteDuration, to: ActorRef[T], message: T): Cancellable =
    schedule(delay, period)(to.timerTell(message))

  override def reportFailure[T: Show](exc: Throwable, ctx: => T)(using Enclosing, Line): Unit =
    logger.error(exc, s"Failure reported via ActorRuntime, provided context: ${ctx.show}")

}
object ActorRuntime {

  // Same as [T] =>> ((String, Behavior[T], ActorParent, ActorRuntime) => ActorFiber[T])
  // But for some reason lambda requires to set type parameter when passed
  trait FiberFactory {

    def apply[T](
                  name: String,
                  behavior:Behavior[T],
                  parent: ActorParent,
                  runtime: ActorRuntime,
                ): ActorFiber[T]

  }

  trait RootHasChilds extends HasChilds.Async {
    this: ActorRuntime =>

    override def rootChilds: Set[ActorRef[Nothing]] = getChilds

    override private[actor] def addRootChild[T](path: ActorPath[T], ref: ActorRef[Nothing]): Unit =
      addChild(path, ref)

    override private[actor] def removeRootChild[T](path: ActorPath[T]): Option[ActorRef[Nothing]] =
      removeChild(path)

    override private[actor] def getRootChild[T](path: ActorPath[T]): Option[ActorRef[Nothing]] =
      getChild(path)

  }

  private class SimpleRuntime(
                               val logger: Logger,
                               val fiberConfig: FiberConfig,
                               val scheduler: Scheduler,
                               val pathFactory: ActorPath.Factory,
                               blockingFactory: FiberFactory,
                               pinnedFactory: FiberFactory,
                               asyncFactory: FiberFactory,
                             ) extends ActorRuntime with RootHasChilds {
    export scheduler.*

    override private[actor] def makeBlocking[T](name: String, behavior: Behavior[T], parent: ActorParent): ActorFiber[T] =
      blockingFactory[T](
        name,
        behavior,
        parent,
        this,
      )

    override private[actor] def makePinned[T](name: String, behavior: Behavior[T], parent: ActorParent): ActorFiber[T] =
      pinnedFactory[T](
        name,
        behavior,
        parent,
        this,
      )

    override private[actor] def makeAsync[T](name: String, behavior: Behavior[T], parent: ActorParent): ActorFiber[T] =
      asyncFactory[T](
        name,
        behavior,
        parent,
        this,
      )

  }

  def apply(
             logger: Logger,
             fiberConfig: FiberConfig,
             scheduler: Scheduler,
             pathFactory: ActorPath.Factory,
             blockingFactory: FiberFactory,
             pinnedFactory: FiberFactory,
             asyncFactory: FiberFactory,
           ): ActorRuntime =
    new SimpleRuntime(
      logger = logger,
      fiberConfig = fiberConfig,
      scheduler = scheduler,
      pathFactory = pathFactory,
      blockingFactory = blockingFactory,
      pinnedFactory = pinnedFactory,
      asyncFactory = asyncFactory,
    )

  private class Default(
                         logLevel: LogLevel,
                         timestampPattern: String,
                         printer: Printer[Id],
                         val pathFactory: ActorPath.Factory,
                         schedulerConfig: SchedulerConfig,
                         val fiberConfig: FiberConfig,
                       ) extends ActorRuntime with RootHasChilds {

    val logger: Logger = RuntimeLogger(logLevel, timestampPattern, printer)

    val scheduler: Scheduler = Scheduler.default(logger, schedulerConfig)

    export scheduler.*

    inline private def makeFiber[T](
                                     inline executionFactory: FiberState[T] => ExecutionStrategy.Factory,
                                     name: String,
                                     behavior: Behavior[T],
                                     parent: ActorParent,
                                   ): ActorFiber[T] =
      val path = pathFactory[T](name, parent)
      val state = FiberState.default[T](
        config.timerThreads,
        fiberConfig,
        parent,
        path,
      )
      val execution = executionFactory(state)
      ActorFiber[T](
        state,
        behavior,
        this,
        this,
        execution,
      )
    end makeFiber

    inline private def makeShifted[T](
                                       ec: ExecutionContext,
                                       name: String,
                                       behavior: Behavior[T],
                                       parent: ActorParent,
                                     ): ActorFiber[T] =
      val fiber = makeFiber[T](
        state => ShiftedExecution[T](
          ec,
          state,
        ),
        name,
        behavior,
        parent,
      )
      fiber.run()
      fiber
    end makeShifted

    override def makeBlocking[T](name: String, behavior: Behavior[T], parent: ActorParent): ActorFiber[T] =
      makeShifted[T](blockingEC, name, behavior, parent)

    override def makeAsync[T](name: String, behavior: Behavior[T], parent: ActorParent): ActorFiber[T] =
      makeShifted[T](this, name, behavior, parent)

    override def makePinned[T](name: String, behavior: Behavior[T], parent: ActorParent): ActorFiber[T] =
      val fiber = makeFiber[T](
        PinnedExecution.apply[T],
        name,
        behavior,
        parent,
      )
      blocking(fiber)
      fiber
    end makePinned
    
  }

  def default(
               logLevel: LogLevel,
               timestampPattern: String = ActorLogger.defaultTimestampPattern,
               printer: Printer[Id] = ActorLogger.defaultPrinter,
               pathFactory: ActorPath.Factory = ActorPath.default,
               schedulerConfig: SchedulerConfig = SchedulerConfig.default,
               fiberConfig: FiberConfig = FiberConfig.default,
             ): ActorRuntime =
    new Default(
      logLevel = logLevel,
      timestampPattern = timestampPattern,
      printer = printer,
      pathFactory = pathFactory,
      schedulerConfig = schedulerConfig,
      fiberConfig = fiberConfig,
    )


}
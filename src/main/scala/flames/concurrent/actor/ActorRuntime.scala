package flames.concurrent.actor

import flames.concurrent.*
import flames.concurrent.actor.fiber.*
import flames.concurrent.actor.*
import flames.concurrent.actor.mailbox.*
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

trait ActorRuntime extends ActorScheduler {

  protected given ActorRuntime = this

  def logger: Logger

  def rootChilds: Set[ActorRef[Nothing]]

  private[actor] def addRootChild[T](path: ActorPath[T], ref: ActorRef[Nothing]): Unit

  private[actor] def removeRootChild[T](path: ActorPath[T]): Option[ActorRef[Nothing]]

  private[actor] def getRootChild[T](path: ActorPath[T]): Option[ActorRef[Nothing]]

  def spawn[Message, Model <: ExecutionModel](actor: Actor[Message, Model]): ActorRef[Message] = {
    actor.run()
    val ref = actor.self
    addRootChild(ref.path, ref)
    ref
  }

  private[actor] def pathFactory: ActorPath.Factory

  private[actor] def fiberFactory: ActorFiber.Factory

  private[actor] def mailboxFactory: Mailbox.Factory

  override def scheduleMessage[T](delay: FiniteDuration, to: ActorRef[T], message: T): Cancellable =
    schedule(delay)(to.timerTell(message))

  override def scheduleMessage[T](delay: FiniteDuration, period: FiniteDuration, to: ActorRef[T], message: T): Cancellable =
    schedule(delay, period)(to.timerTell(message))

  override def reportFailure[T: Show](exc: Throwable, ctx: => T)(using Enclosing, Line): Unit =
    logger.error(exc, s"Failure reported via ActorRuntime, provided context: ${ctx.show}")

}
object ActorRuntime {

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

  private class Simple(
                        val logger: Logger,
                        val scheduler: Scheduler,
                        val config: ActorsConfig,
                        val pathFactory: ActorPath.Factory,
                        val mailboxFactory: Mailbox.Factory,
                        val fiberFactory: ActorFiber.Factory,
                      ) extends ActorRuntime with RootHasChilds {
    export scheduler.{config as _, *}

  }

  def apply(
             logger: Logger,
             scheduler: Scheduler,
             config: ActorsConfig,
             pathFactory: ActorPath.Factory,
             mailboxFactory: Mailbox.Factory,
             fiberFactory: ActorFiber.Factory,
           ): ActorRuntime =
    new Simple(
      logger = logger,
      scheduler = scheduler,
      config = config,
      pathFactory = pathFactory,
      mailboxFactory = mailboxFactory,
      fiberFactory = fiberFactory,
    )

  private class Default(
                         logLevel: LogLevel,
                         timestampPattern: String,
                         printer: Printer[Id],
                         val config: ActorsConfig,
                         val pathFactory: ActorPath.Factory,
                         val mailboxFactory: Mailbox.Factory,
                         val fiberFactory: ActorFiber.Factory,
                       ) extends ActorRuntime with RootHasChilds {

    val logger: RuntimeLogger = RuntimeLogger(logLevel, timestampPattern, printer)

    logger.run()

    val scheduler: Scheduler = Scheduler.default(logger, config)

    export scheduler.{config as _, *}
    
  }

  def default(
               logLevel: LogLevel,
               timestampPattern: String = ActorLogger.defaultTimestampPattern,
               printer: Printer[Id] = ActorLogger.defaultPrinter,
               config: ActorsConfig = ActorsConfig.default,
               pathFactory: ActorPath.Factory = ActorPath.defaultFactory,
               mailboxFactory: Mailbox.Factory = Mailbox.defaultFactory,
               fiberFactory: ActorFiber.Factory = ActorFiber.defaultFactory,
             ): ActorRuntime =
    new Default(
      logLevel = logLevel,
      timestampPattern = timestampPattern,
      printer = printer,
      config = config,
      pathFactory = pathFactory,
      mailboxFactory = mailboxFactory,
      fiberFactory = fiberFactory,
    )


}
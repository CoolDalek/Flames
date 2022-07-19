package flames.concurrent.actor

import flames.concurrent.execution.SchedulerConfig

import scala.Option.option2Iterable
import scala.concurrent.duration.FiniteDuration

trait ActorsConfig extends SchedulerConfig {

  def autoYieldCount: Int

  def autoYieldTime: Option[FiniteDuration]

  def queueMaxSize: Int

  def queueInitSize: Int

}
object ActorsConfig {

  class Simple(
                val minBlockingThreads: Int,
                val maxBlockingThreads: Int,
                val minComputeThreads: Int,
                val maxComputeThreads: Int,
                val timerThreads: Int,
                val keepAlive: FiniteDuration,
                val interruptOnCancel: Boolean,
                val autoYieldCount: Int,
                val autoYieldTime: Option[FiniteDuration],
                val queueMaxSize: Int,
                val queueInitSize: Int,
              ) extends ActorsConfig

  def apply(
             minBlockingThreads: Int = 0,
             maxBlockingThreads: Int = Int.MaxValue,
             minComputeThreads: Int = SchedulerConfig.availableProcessors,
             maxComputeThreads: Int = SchedulerConfig.availableProcessors,
             timerThreads: Int = 1,
             keepAlive: FiniteDuration = SchedulerConfig.defaultKeepAlive,
             interruptOnCancel: Boolean = false,
             autoYieldCount: Int = 8,
             autoYieldTime: Option[FiniteDuration] = None,
             queueMaxSize: Int = 1024,
             queueInitSize: Int = 16,
           ): ActorsConfig =
    new Simple(
      minBlockingThreads = minBlockingThreads,
      maxBlockingThreads = maxBlockingThreads,
      minComputeThreads = minComputeThreads,
      maxComputeThreads = maxComputeThreads,
      timerThreads = timerThreads,
      keepAlive = keepAlive,
      interruptOnCancel = interruptOnCancel,
      autoYieldCount = autoYieldCount,
      autoYieldTime = autoYieldTime,
      queueMaxSize = queueMaxSize,
      queueInitSize = queueInitSize,
    )

  val default: ActorsConfig = apply()

}

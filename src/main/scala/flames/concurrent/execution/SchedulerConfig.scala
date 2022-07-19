package flames.concurrent.execution

import flames.logging.Logger

import scala.concurrent.duration.*
import SchedulerConfig.*

trait SchedulerConfig {

  def minBlockingThreads: Int

  def maxBlockingThreads: Int

  def minComputeThreads: Int

  def maxComputeThreads: Int

  def timerThreads: Int

  def keepAlive: FiniteDuration

  def interruptOnCancel: Boolean

}
object SchedulerConfig {

  private class Simple(
                        val minBlockingThreads: Int, 
                        val maxBlockingThreads: Int, 
                        val minComputeThreads: Int, 
                        val maxComputeThreads: Int, 
                        val timerThreads: Int, 
                        val keepAlive: FiniteDuration, 
                        val interruptOnCancel: Boolean,
                      ) extends SchedulerConfig

  val availableProcessors: Int = sys.runtime.availableProcessors
  val defaultKeepAlive: FiniteDuration = 60.seconds

  def apply(
             minBlockingThreads: Int = 0,
             maxBlockingThreads: Int = Int.MaxValue, 
             minComputeThreads: Int = availableProcessors, 
             maxComputeThreads: Int = availableProcessors, 
             timerThreads: Int = 1, 
             keepAlive: FiniteDuration = defaultKeepAlive, 
             interruptOnCancel: Boolean = false,
           ): SchedulerConfig =
    new Simple(
      minBlockingThreads = minBlockingThreads,
      maxBlockingThreads = maxBlockingThreads,
      minComputeThreads = minComputeThreads,
      maxComputeThreads = maxComputeThreads,
      timerThreads = timerThreads,
      keepAlive = keepAlive,
      interruptOnCancel = interruptOnCancel,
    )
  
  val default: SchedulerConfig = apply()

}
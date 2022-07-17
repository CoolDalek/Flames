package flames.concurrent.execution

import flames.logging.Logger

import scala.concurrent.duration.*
import SchedulerConfig.*

case class SchedulerConfig(
                            minBlockingThreads: Int = 0,
                            maxBlockingThreads: Int = Int.MaxValue,
                            minComputeThreads: Int = availableProcessors,
                            maxComputeThreads: Int = availableProcessors,
                            timerThreads: Int = 1,
                            keepAlive: FiniteDuration = defaultKeepAlive,
                            interruptOnCancel: Boolean = false,
                          )
object SchedulerConfig {

  val availableProcessors: Int = sys.runtime.availableProcessors
  val defaultKeepAlive: FiniteDuration = 60.seconds

  val default: SchedulerConfig = SchedulerConfig()

}
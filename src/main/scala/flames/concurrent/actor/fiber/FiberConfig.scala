package flames.concurrent.actor.fiber

import scala.concurrent.duration.FiniteDuration

case class FiberConfig(
                        userQueueSize: Int = 1024,
                        timerQueueSize: Int = 1024,
                        systemQueueSize: Int = 1024,
                        autoYieldCount: Int = 8,
                        autoYieldTime: Option[FiniteDuration] = None,
                      )
object FiberConfig {
  
  val default: FiberConfig = FiberConfig()
  
}
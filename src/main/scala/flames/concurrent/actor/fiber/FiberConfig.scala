package flames.concurrent.actor.fiber

import scala.concurrent.duration.FiniteDuration

case class FiberConfig(
                        userChunkSize: Int = 64,
                        timerChunkSize: Int = 64,
                        systemChunkSize: Int = 64,
                        autoYieldCount: Int = 8,
                        autoYieldTime: Option[FiniteDuration] = None,
                      )
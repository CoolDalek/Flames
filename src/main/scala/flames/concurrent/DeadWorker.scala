package flames.concurrent

import scala.util.control.NoStackTrace

case object DeadWorker extends Exception(
  s"Trying to process stopped worker."
) with NoStackTrace
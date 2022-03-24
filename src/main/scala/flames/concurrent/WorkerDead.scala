package flames.concurrent

import scala.util.control.NoStackTrace

case object WorkerDead extends Exception(
  "Trying to process stopped worker."
) with NoStackTrace
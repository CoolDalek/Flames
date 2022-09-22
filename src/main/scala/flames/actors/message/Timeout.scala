package flames.actors.message

import scala.concurrent.duration.*

object Timeout {
  opaque type Timeout = FiniteDuration

  object limit

  given DurationConversions.Classifier[limit.type] with
    override type R = Timeout

    inline override def convert(d: FiniteDuration): Timeout = d
  end given

  extension (self: Timeout) {

    inline def asDuration: FiniteDuration = self

  }

  inline def fromDuration(duration: FiniteDuration): Timeout = duration

}

export Timeout.Timeout
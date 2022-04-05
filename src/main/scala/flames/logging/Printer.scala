package flames.logging

import flames.util.{Show, SummonerK}

trait Printer[F[_]] {

  def print[T: Show](obj: T): F[Unit]

  def println[T: Show](obj: T): F[Unit]

}
object Printer extends SummonerK[Printer]
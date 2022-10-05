package flames.concurrent.actors.remote

import flames.concurrent.actors.*
import flames.concurrent.actors.path.*
import flames.concurrent.actors.message.*
import flames.concurrent.actors.pattern.Wait
import flames.concurrent.utils.*

import scala.annotation.tailrec

trait Client {

  def select[F[_]: Wait](query: Vector[ActorSelector],
                         credentials: ActorSelector.Remote)
                        (using Timeout): F[Ack[SelectionResult[Nothing]]]

  def shutdown(): Unit

}
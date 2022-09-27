package flames.actors.remote

import flames.actors.path.{ActorSelector, SelectionResult}
import flames.actors.message.Timeout
import flames.actors.pattern.Wait

trait Client {

  def select[F[_]: Wait](query: Vector[ActorSelector],
                         credentials: ActorSelector.Remote)
                        (using Timeout): F[SelectionResult[Nothing]]

}

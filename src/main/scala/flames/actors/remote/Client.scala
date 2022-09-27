package flames.actors.remote

import flames.actors.*
import flames.actors.path.{ActorSelector, SelectionResult}
import flames.actors.message.*
import flames.actors.pattern.Wait

trait Client {

  def select[F[_]: Wait](query: Vector[ActorSelector],
                         credentials: ActorSelector.Remote)
                        (using Timeout): F[Ack[SelectionResult[Nothing]]]

  def shutdown(): Unit

}
object Client {

  type Completer = DeliveryFailure | SelectionResult[Nothing]
  enum Protocol {
    case SelectRequest(
                        query: Vector[ActorSelector],
                        credentials: ActorSelector.Remote,
                        complete: Completer,
                      )
    case SelectResponse
    case ResponseTimeout
    case Tick
    case Shutdown
  }
  protected class Impl(using ActorEnv[Protocol]) extends Client with Actor[Protocol]("remote-connector"):
    import Protocol.*

    override def select[F[_] : Wait](query: Vector[ActorSelector],
                                     credentials: ActorSelector.Remote)
                                    (using Timeout): F[Ack[SelectionResult[Nothing]]] =
      Wait[F].asyncAck { callback =>
        self.tell(
          SelectRequest(query, credentials, callback)
        )
      }
    end select

    import scala.collection.mutable
    import java.nio.*
    import java.nio.channels.*
    import java.net.{InetSocketAddress, StandardSocketOptions}

    private val active = mutable.HashMap[Long, Completer]
    private val eventLoop = Selector.open()

    override protected def act(): Behavior[Protocol] = idle

    def handle(req: SelectRequest): Unit =
      val creds = req.credentials
      val socket = SocketChannel.open(
        new InetSocketAddress(creds.host, creds.port)
      )
      socket.configureBlocking(false)
      socket.setOption(StandardSocketOptions.TCP_NODELAY, true)
      socket.register(eventLoop, socket.validOps())
    end handle

    def idle: Behavior[Protocol] =
      receive {
        case start: SelectRequest =>
        case Shutdown => stop
      }.ignore
    end idle

    def working: Behavior[Protocol] =
      ???
    end working

  end Impl

}
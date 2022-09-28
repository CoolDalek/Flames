package flames.actors.remote

import flames.actors.*
import flames.actors.path.*
import flames.actors.message.*
import flames.actors.pattern.Wait
import flames.actors.utils.*

import scala.annotation.tailrec

trait Client {

  def select[F[_]: Wait](query: Vector[ActorSelector],
                         credentials: ActorSelector.Remote)
                        (using Timeout): F[Ack[SelectionResult[Nothing]]]

  def shutdown(): Unit

}
object Client {

  import scala.collection.mutable
  import java.nio.*
  import java.nio.channels.*
  import java.net.*
  import scala.util.control.*

  type Completer = DeliveryFailure | SelectionResult[Nothing] => Unit
  enum Protocol {
    case SelectRequest(
                        query: Vector[ActorSelector],
                        credentials: ActorSelector.Remote,
                        complete: Completer,
                        timeout: Timeout,
                      )
    case SelectResponse
    case ResponseTimeout(key: SelectionKey)
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

    private val active = mutable.Map.empty[SelectionKey, Completer]
    private val pending = mutable.Map.empty[SelectionKey, Vector[ActorSelector]]
    private val eventLoop = Selector.open()

    override protected def act(): Behavior[Protocol] = idle

    inline private def guard[R](complete: Completer, inline cleanup: => Unit = () => ())
                               (inline action: => R): Unit =
      try action
      catch case NonFatal(exc) =>
        complete(DeliveryFailure.Connection(exc))
        cleanup
    end guard

    def handle(req: SelectRequest): Unit =
      val creds = req.credentials
      val complete = req.complete
      guard(complete) {
        val socket = SocketChannel.open(
          new InetSocketAddress(creds.host, creds.port)
        )
        socket.configureBlocking(false)
        socket.setOption(StandardSocketOptions.TCP_NODELAY, true)
        val key = socket.register(eventLoop, socket.validOps())
        active.update(key, complete)
        pending.update(key, req.query)
        scheduleToSelf(req.timeout.asDuration, ResponseTimeout(key))
      }
    end handle

    def idle: Behavior[Protocol] =
      receive {
        case req: SelectRequest =>
          handle(req)
          if(active.isEmpty) same
          else
            self.tell(Tick)
            working
        case Shutdown =>
          eventLoop.close()
          stop
        case _ => same
      }.ignore
    end idle

    def working: Behavior[Protocol] =
      receive {
        case req: SelectRequest =>
          handle(req)
          same
        case ResponseTimeout(key) =>
          key.cancel()
          active.remove(key).foreach(_(DeliveryFailure.TimedOut))
          same
        case Shutdown =>
          eventLoop.close()
          active.foreach { (_, complete) =>
            complete(DeliveryShutdown)
          }
          active.clear()
          stop
        case Tick =>
          self.tell(Tick)
          eventLoop.selectNow { key =>
            if(key.isWritable && pending.contains(key))
              val complete = active(key)
              val query = pending.remove(key).get
              guard(complete, active.remove(key)) {
                val data = Impl.serializeQuery(query)
                key.channel()
                  .asInstanceOf[SocketChannel]
                  .write(data)
              }
            else if(key.isReadable && !pending.contains(key)) {
              val complete = active(key)
              guard(complete, active.remove(key)) {
                val data = key.channel()
                  .asInstanceOf[SocketChannel]
                  .read()
              }
            }
          }
          same
      }.ignore
    end working

  private object Impl:

    def serializeQuery(query: Vector[ActorSelector]): Array[ByteBuffer] = {
      val array = new Array[ByteBuffer](query.length)

      def uniqueString(x: Unique | Null): String =
        x.mapOrElse(
          y => y.
        )
      end uniqueString

      @tailrec
      def loop(i: Int): Unit =
        if(query.length < i)
          query(i) match {
            case ActorSelector.Simple(name, unique) =>
              array(i) = ByteBuffer.wrap(
                s"Simple($name, ${unique.ma})".getBytes
              )
            case ActorSelector.Remote(name, unique, host, port) => ???
          }
          loop(i + 1)
      end loop
      loop(0)
      array
    }


  end Impl

  private val DeliveryShutdown: DeliveryFailure = DeliveryFailure.Connection(ConnectionShutdown)

  object ConnectionShutdown extends RuntimeException("Connection shutdown") with NoStackTrace

}
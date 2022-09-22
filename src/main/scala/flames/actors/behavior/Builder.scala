package flames.actors.behavior

import flames.actors.message.SystemMessage

import scala.annotation.targetName
import scala.util.control.NonFatal

sealed trait Builder[-Protocol, -ReceiveType]
object Builder {

  case class Receive[Protocol, ReceiveType](
                                             act: Receiver[Protocol, ReceiveType],
                                           ) extends Builder[Protocol, ReceiveType] {

    def onFailure(handler: PartialFunction[Throwable, Behavior[Protocol]]): Builder[Protocol, ReceiveType] =
      HandleFailure(this, handler)

  }

  case class HandleFailure[Protocol, ReceiveType](
                                                   receive: Receive[Protocol, ReceiveType],
                                                   handler: PartialFunction[Throwable, Behavior[Protocol]],
                                                 ) extends Builder[Protocol, ReceiveType]

  case object Ignore extends Builder[Any, Any]

  type ReceiveProtocol[T] = Receive[T, T]
  type ReceiveSystem[T] = Receive[T, SystemMessage]

  extension [T](protocol: Builder[T, T]) {

    @targetName("andSystem")
    inline def and(system: Builder[T, SystemMessage]): Behavior[T] =
      build(
        protocol = protocol,
        system = system,
      )

    @targetName("ignoreSystem")
    inline def ignore: Behavior[T] = and(Ignore)

  }

  extension [T](system: Builder[T, SystemMessage]) {

    @targetName("andProtocol")
    inline def and(protocol: Builder[T, T]): Behavior[T] =
      build(
        protocol = protocol,
        system = system,
      )

    @targetName("ignoreProtocol")
    inline def ignore: Behavior[T] = and(Ignore)

  }

  private inline def build[T](
                               protocol: Builder[T, T],
                               system: Builder[T, SystemMessage],
                             ): Behavior[T] =
    Behavior.Receive(
      actProtocol = interpret(protocol),
      actSystem = interpret(system),
    )
  end build

  def interpret[Protocol, ReceiveType](
                                        builder: Builder[Protocol, ReceiveType]
                                      ): Receiver[Protocol, ReceiveType] =
    builder match
      case Receive(act) => act
      case HandleFailure(receive, handler) =>
        val act = receive.act
        (msg: ReceiveType) => {
          try act(msg)
          catch case NonFatal(exc) =>
            handler.applyOrElse(exc, _ => throw exc)
        }
      case Ignore => Receiver.Ignore
  end interpret

}
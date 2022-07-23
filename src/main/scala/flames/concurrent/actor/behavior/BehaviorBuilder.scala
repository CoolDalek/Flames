package flames.concurrent.actor.behavior

import flames.concurrent.actor.mailbox.SystemMessage

import scala.annotation.{targetName, switch}
import scala.util.control.NonFatal
import scala.util.NotGiven

import BehaviorBuilder.*
import BuilderTag.*

type ReceiveProtocol[T] = BehaviorBuilder.Receive[T, T]
type ReceiveSystem[T] = BehaviorBuilder.Receive[T, SystemMessage]

sealed trait BehaviorBuilder[+Protocol, +ReceiveType] {

  def tag: BuilderTag

}
object BehaviorBuilder {

  extension [T](self: BehaviorBuilder[T, T]) {

    @targetName("andSystem")
    inline def and(other: BehaviorBuilder[T, SystemMessage]): Behavior[T] =
      build(
        system = other,
        protocol = self,
      )

    inline def ignoreSystem: Behavior[T] = and(Ignore)

  }

  extension [T](self: BehaviorBuilder[T, SystemMessage]) {

    @targetName("andProtocol")
    inline def and(other: BehaviorBuilder[T, T]): Behavior[T] =
      build(
        system = self,
        protocol = other,
      )

    inline def ignoreProtocol: Behavior[T] = and(Ignore)

  }

  class Receive[Protocol, ReceiveType](
                                        val act: ReceiveType => Behavior[Protocol]
                                      ) extends BehaviorBuilder[Protocol, ReceiveType] {


    override val tag: BuilderTag = ReceiveTag

    def onFailure(handler: PartialFunction[Throwable, Behavior[Protocol]]): BehaviorBuilder[Protocol, ReceiveType] =
      HandleFailure(handler, this)

  }

  class HandleFailure[Protocol, ReceiveType](
                                              val handler: PartialFunction[Throwable, Behavior[Protocol]],
                                              val receive: Receive[Protocol, ReceiveType],
                                            ) extends BehaviorBuilder[Protocol, ReceiveType] {
    override val tag: BuilderTag = HandleFailureTag
  }


  object Ignore extends BehaviorBuilder[Nothing, Nothing] {
    override val tag: BuilderTag = IgnoreTag
  }

  private inline def build[T](
                               system: BehaviorBuilder[T, SystemMessage],
                               protocol: BehaviorBuilder[T, T],
                             ): Behavior[T] =
    Behavior.Receive[T](
      act = interpret(protocol),
      actSystem = interpret(system),
    )
  end build

  private def interpret[Protocol, ReceiveType](
                                                builder: BehaviorBuilder[Protocol, ReceiveType],
                                              ): GenericAct[Protocol, ReceiveType] =
    (builder.tag: @switch) match {
      case ReceiveTag =>
        val self = builder.asInstanceOf[Receive[Protocol, ReceiveType]]
        self.act.asInstanceOf[GenericAct[Protocol, ReceiveType]]
      case HandleFailureTag =>
        val self = builder.asInstanceOf[HandleFailure[Protocol, ReceiveType]]
        val handler = self.handler
        val act = self.receive.act
        (msg: ReceiveType) => try {
          act(msg)
        } catch {
          case NonFatal(exc) =>
            if(handler.isDefinedAt(exc)) handler(exc)
            else throw exc
        }
      case IgnoreTag =>
        null
    }

}
package flames.concurrent.actor.fiber

import flames.concurrent.actor.*

import scala.collection.concurrent.TrieMap
import scala.collection.mutable

trait HasChilds {

  def addChild[T](token: ActorPath[T], actor: ActorRef[Nothing]): Unit

  def removeChild[T](token: ActorPath[T]): Option[ActorRef[Nothing]]

  def getChild[T](token: ActorPath[T]): Option[ActorRef[Nothing]]

  def getChilds: Set[ActorRef[Nothing]]

}
object HasChilds {

  trait ScalaMap extends HasChilds {

    protected def childs: mutable.Map[ActorPath[Nothing], ActorRef[Nothing]]

    override def addChild[T](token: ActorPath[T], actor: ActorRef[Nothing]): Unit =
      childs.update(token, actor)

    override def removeChild[T](token: ActorPath[T]): Option[ActorRef[Nothing]] =
      childs.remove(token)

    override def getChilds: Set[ActorRef[Nothing]] =
      childs.values to Set

    override def getChild[T](token: ActorPath[T]): Option[ActorRef[Nothing]] =
      childs.get(token)

  }

  trait Sync extends ScalaMap {

    final override protected val childs = mutable.HashMap.empty[ActorPath[Nothing], ActorRef[Nothing]]

  }

  trait Async extends ScalaMap {

    final override protected val childs = TrieMap.empty[ActorPath[Nothing], ActorRef[Nothing]]

  }

}
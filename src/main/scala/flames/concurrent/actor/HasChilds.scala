package flames.concurrent.actor

import flames.concurrent.actor.*

import scala.collection.concurrent.TrieMap
import scala.collection.mutable

trait HasChilds {

  def addChild[T](path: ActorPath[T], actor: ActorRef[T]): Unit

  def removeChild[T](path: ActorPath[T]): Option[ActorRef[Nothing]]

  def getChild[T](path: ActorPath[T]): Option[ActorRef[Nothing]]

  def getChilds: Set[ActorRef[Nothing]]

}
object HasChilds {

  trait ScalaMap extends HasChilds {

    protected def childs: mutable.Map[ActorPath[Nothing], ActorRef[Nothing]]

    override def addChild[T](path: ActorPath[T], actor: ActorRef[T]): Unit =
      childs.update(path, actor)

    override def removeChild[T](path: ActorPath[T]): Option[ActorRef[Nothing]] =
      childs.remove(path)

    override def getChilds: Set[ActorRef[Nothing]] =
      childs.values to Set

    override def getChild[T](path: ActorPath[T]): Option[ActorRef[Nothing]] =
      childs.get(path)

  }

  trait Sync extends ScalaMap {

    final override protected val childs = mutable.HashMap.empty[ActorPath[Nothing], ActorRef[Nothing]]

  }

  trait Async extends ScalaMap {

    final override protected val childs = TrieMap.empty[ActorPath[Nothing], ActorRef[Nothing]]

  }

}
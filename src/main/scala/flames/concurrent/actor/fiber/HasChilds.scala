package flames.concurrent.actor.fiber

import flames.concurrent.actor.*

import scala.collection.concurrent.TrieMap
import scala.collection.mutable

trait HasChilds {

  def addChild(token: ActorToken, actor: ActorRef[Nothing]): Unit

  def removeChild(token: ActorToken): Option[ActorRef[Nothing]]

  def getChild(token: ActorToken): Option[ActorRef[Nothing]]

  def getChilds: Set[ActorRef[Nothing]]

}
object HasChilds {

  trait ScalaMap extends HasChilds {

    protected def childs: mutable.Map[ActorToken, ActorRef[Nothing]]

    override def addChild(token: ActorToken, actor: ActorRef[Nothing]): Unit =
      childs.update(token, actor)

    override def removeChild(token: ActorToken): Option[ActorRef[Nothing]] =
      childs.remove(token)

    override def getChilds: Set[ActorRef[Nothing]] =
      childs.values to Set

    override def getChild(token: ActorToken): Option[ActorRef[Nothing]] =
      childs.get(token)

  }

  trait Sync extends ScalaMap {

    final override protected val childs = mutable.HashMap.empty[ActorToken, ActorRef[Nothing]]

  }

  trait Async extends ScalaMap {

    final override protected val childs = TrieMap.empty[ActorToken, ActorRef[Nothing]]

  }

}
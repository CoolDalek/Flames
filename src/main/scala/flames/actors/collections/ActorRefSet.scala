package flames.actors.collections

import flames.actors.*
import flames.actors.path.*

import scala.reflect.ClassTag

trait ActorRefSet[T] {

  def add(ref: ActorRef[T]): Boolean

  def remove(path: ActorPath): ActorRef[T] | Null

  def isEmpty: Boolean

  def nonEmpty: Boolean

  def foreach(consumer: ActorRef[T] => Unit): Unit

  def size: Int

  def length: Int

}

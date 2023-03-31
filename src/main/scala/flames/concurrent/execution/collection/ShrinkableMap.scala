package flames.concurrent.execution.collection

import scala.annotation.tailrec
import flames.concurrent.utils.*


trait ShrinkableMap[K, V]:

  def contains(key: K)(using Empty[V]): Boolean

  def get(key: K)(using Empty[V]): Maybe[V]

  def update(key: K, value: V)(using Empty[V]): Maybe[V]

  def remove(key: K)(using Empty[V]): Maybe[V]

  def size: Int

  def foreach(f: (K, V) => Unit): Unit

object ShrinkableMap:
  def radix16[K, V]: ShrinkableMap[K, V] = ???

end ShrinkableMap

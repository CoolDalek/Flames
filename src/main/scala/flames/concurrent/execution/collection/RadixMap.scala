package flames.concurrent.execution.collection

import flames.concurrent.utils.*

class Root[K, V] extends ShrinkableMap[K, V]:

  var values: Array[ArrayNode[K, V]] = new Array(16)

  def contains(key: K)(using Empty[V]): Boolean = get(key).nonEmpty

  def get(key: K)(using Empty[V]): Maybe[V] = ???

  def update(key: K, value: V)(using Empty[V]): Maybe[V] =
    val hash = RadixNode.evalHash(key)
    val position = RadixNode.index(hash, 0, 16)
    val stored = values(position)
    if stored eq null then
      val link = new LinkedNode(key, value)
      val array = new ArrayNode(link)
      values(position) = array
      Maybe.absent
    else
      var cursor = stored.self
      var prev = null.asInstanceOf[LinkedNode[K, V]]
      while (cursor ne null) && cursor.key != key do
        prev = cursor
        cursor = cursor.next
      end while
      if cursor eq null then
        prev.next = new LinkedNode(key, value)
        stored.grow()
        Maybe.absent
      else
        val old = cursor.value
        cursor.value = value
        Maybe.present(old)
      end if
    end if
  end update

  def remove(key: K)(using Empty[V]): Maybe[V] = ???

  def size: Int = ???

  def foreach(f: (K, V) => Unit): Unit = ???

trait RadixNode

class LinkedNode[K, V](
                        val key: K,
                        var value: Maybe[V],
                        var next: LinkedNode[K, V] = null,
                      ) extends RadixNode

class ArrayNode[K, V](
                       var self: LinkedNode[K, V],
                       var load: Int = 0,
                       var childs: Array[ArrayNode[K, V]] = null,
                     ) extends RadixNode
  

object RadixNode:

  val Masks: Array[Int] = Array(
    15, // 0000_0000_0000_0000_0000_0000_0000_1111
    240, // 0000_0000_0000_0000_0000_0000_1111_0000
    3840, // 0000_0000_0000_0000_0000_1111_0000_0000
    61440, // 0000_0000_0000_0000_1111_0000_0000_0000
    983040, // 0000_0000_0000_1111_0000_0000_0000_0000
    15728640, // 0000_0000_1111_0000_0000_0000_0000_0000
    251658240, // 0000_1111_0000_0000_0000_0000_0000_0000
    -268435456, // 1111_0000_0000_0000_0000_0000_0000_0000
  )

  val Shifts: Array[Int] = Array(
    0, 4, 8, 12,
    16, 20, 24, 28,
  )

  inline def index(hash: Int, level: Int, size: Int): Int =
    (hash >>> Shifts(level) & (size - 1)

  inline def evalHash[K](key: K): Int =
    val hash = key.##
    hash ^ (hash >>> 16)

end RadixNode

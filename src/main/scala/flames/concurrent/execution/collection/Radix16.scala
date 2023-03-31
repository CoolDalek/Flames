package flames.concurrent.execution.collection

import flames.concurrent.utils.*
import Radix16.*

import scala.annotation.switch

class Radix16[K, V] extends ShrinkableMap[K, V]:

  def contains(key: K)(using Empty[V]): Boolean = get(key).nonEmpty

  def get(key: K)(using Empty[V]): Maybe[V] = ???

  def update(key: K, value: V)(using Empty[V]): Maybe[V] = ???

  def remove(key: K)(using Empty[V]): Maybe[V] = ???

  def size: Int = ???

  def foreach(f: (K, V) => Unit): Unit = ???

object Radix16:

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
    ((hash & Masks(level)) >>> Shifts(level)) & size

  inline def evalHash[K](key: K): Int =
    val hash = key.##
    hash ^ (hash >>> 16)

  object Tag:
    type Branch = 0
    inline val Branch: Branch = 0
    type Leaf = 1
    inline val Leaf: Leaf = 1
    type Tree = Branch | Leaf
  end Tag

  sealed trait Tree[K, V]:
    def get(hash: Int, key: K, level: Int)(using Empty[V]): Maybe[V]
    def tag: Tag.Tree

  sealed trait Branch[K, V] extends Tree[K, V]:

    final def tag: Tag.Branch = Tag.Branch

    def update(pos: Int, value: Tree[K, V]): Unit = ???

    def size: Int

    def getChild(index: Int): Tree[K, V] | Null

    def insert(index: Int, key: K, value: V): Unit

    def grow(hash: Int, key: K, value: V, level: Int, selfPos: Int, parent: Branch[K, V], childPos: Int, child: Leaf[K, V]): Unit = ???

    private inline def index(hash: Int, level: Int): Int = Radix16.index(hash, level, size)

    final def get(hash: Int, key: K, level: Int)(using Empty[V]): Maybe[V] =
      val i = index(hash, level)
      val child = getChild(i)
      if child ne null
      then child.get(hash, key, level + 1)
      else Maybe.absent[V]
    end get

    final def update(hash: Int, key: K, value: V,
                     lvl: Int, pos: Int, parent: Branch[K, V])
                    (using Empty[V]): Maybe[V] =
      val i = index(hash, lvl)
      val child = getChild(i)
      if child ne null then
        (child.tag: @switch) match
          case Tag.Leaf =>
            val leaf = child.asInstanceOf[Leaf[K, V]]
            val leafHash = evalHash(leaf.key)
            if leafHash == hash
            then leaf.update(key, value)
            else grow(hash, key, value, lvl, pos, parent, i, leaf); Maybe.absent[V]
          case Tag.Branch =>
            val branch = child.asInstanceOf[Branch[K, V]]
            branch.update(hash, key, value, lvl + 1, i, this)
      else insert(i, key, value)
      Maybe.absent[V]
    end update

  end Branch

  final class Branch2[K, V](
                             var one: Tree[K, V] | Null = null,
                             var two: Tree[K, V] | Null = null,
                           ) extends Branch[K, V]:
    def size: Int = 2

    def getChild(index: Int): Tree[K, V] | Null =
      if index == 0 then one
      else if index == 1 then two
      else throw IndexOutOfBoundsException(index)

    def insert(index: Int, key: K, value: V): Unit =
      if index == 0 then one = Leaf(key, value)
      else if index == 1 then two = Leaf(key, value)
      else throw IndexOutOfBoundsException(index)


  final class Branch4[K, V](
                             var one: Tree[K, V] | Null = null,
                             var two: Tree[K, V] | Null = null,
                             var three: Tree[K, V] | Null = null,
                             var four: Tree[K, V] | Null = null,
                           ) extends Branch[K, V]:
    def size: Int = 4

    def getChild(index: Int): Tree[K, V] | Null =
      if index == 0 then one
      else if index == 1 then two
      else if index == 2 then three
      else if index == 3 then four
      else throw IndexOutOfBoundsException(index)

    def insert(index: Int, key: K, value: V): Unit =
      if index == 0 then one = Leaf(key, value)
      else if index == 1 then two = Leaf(key, value)
      else if index == 2 then three = Leaf(key, value)
      else if index == 3 then four = Leaf(key, value)
      else throw IndexOutOfBoundsException(index)

  sealed trait ArrayBranch[K, V] extends Branch[K, V]:
    val childs: Array[Tree[K, V]]

    def size: Int = childs.length

    def getChild(index: Int): Tree[K, V] | Null = childs(index)

    def insert(index: Int, key: K, value: V): Unit = childs(index) = Leaf(key, value)

  final class Branch8[K, V](
                             val childs: Array[Tree[K, V]] = new Array(8),
                           ) extends ArrayBranch[K, V]

  final class Branch16[K, V](
                              val childs: Array[Tree[K, V]] = new Array(16),
                            ) extends ArrayBranch[K, V]

  final class Leaf[K, V](
                          val key: K,
                          var value: V,
                          var next: Leaf[K, V] | Null = null,
                        ) extends Tree[K, V]:

    def tag: Tag.Leaf = Tag.Leaf

    def update(key: K, value: V)
              (using Empty[V]): Maybe[V] =
      var cursor = this
      var loop = true
      var result = Maybe.absent[V]
      while loop do
        if cursor.key == key then
          result = Maybe.present(cursor.value)
          cursor.value = value
          loop = false
        if cursor.next eq null then
          result.ifAbsent {
            cursor.next = Leaf(key, value)
          }
          loop = false
        cursor = cursor.next
      end while
      result
    end update

    def get(hash: Int, key: K, level: Int)(using Empty[V]): Maybe[V] =
      val self = evalHash(key)
      if self == hash then
        var result = Maybe.absent[V]
        var cursor = this
        var loop = true
        while loop do
          if cursor.key == key then
            result = Maybe.present(cursor.value)
            loop = false
          if cursor.next eq null
          then loop = false
          else cursor = cursor.next
        end while
        result
      else Maybe.absent[V]
    end get

  end Leaf

end Radix16

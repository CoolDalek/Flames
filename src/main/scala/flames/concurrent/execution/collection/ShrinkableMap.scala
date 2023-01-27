package flames.concurrent.execution.collection

import scala.annotation.tailrec
import flames.concurrent.utils.*

trait ShrinkableMap[K, V]:
  def contains(name: K)(using Empty[V]): Boolean
  def get(name: K)(using Empty[V]): Maybe[V]
  def update(name: K, index: V)(using Empty[V]): Maybe[V]
  def remove(name: K)(using Empty[V]): Maybe[V]

object ShrinkableMap:

  /*
  * Implementation based on the scala.collection.Vector internal structure.
  * Its basically a tree where each node can be either a branch or a leaf.
  * Branch nodes contains an array which can be filled with both branches and leaf nodes.
  * Leaf nodes contains an linked-list of key-value pairs with collided hashes.
  * If branch contains less than one element it thies to shrink
  * - it moves it's only element to parent branch and becomes garbage.
  * If new element tries to take place of other element in the branch and their hashes are different
  * - new branch is being created and both elements are being moved here; new branch are placed in a conflicting place.
  * If new element tries to take place of other element in the branch and their hashes are the same
  * - it being added to the end of linked list.
  * All operations traverse tree without stack-consuming recursion.
  * It's can be seen as obsolete since recursion depth will never be nearly enough to cause stack overflow.
  * But it's a conscious design choice made to allow using this collection
  * with stack-consuming async-await implementations.
  * */
  private class Impl[K, V](branchSize: Int) extends ShrinkableMap[K, V]:

    private def evalHash(key: K): Int =
      val original = key.##
      original ^ (original >>> 16)
    end evalHash

    inline private def index(hash: Int, prevPos: Int) =
      (hash + prevPos + 1) & (branchSize - 1)

    private sealed trait Tree

    private class Branch(
                          val childs: Array[Tree] = new Array(branchSize),
                          var load: Int = 1,
                        ) extends Tree:

      def insert(key: K, value: V, position: Int): Unit =
        childs(position) = Leaf(key, value)
        load += 1

    end Branch

    private class Leaf(
                        val key: K,
                        var value: V,
                        var next: Leaf | Null = null,
                      ) extends Tree

    private val root = new Branch()

    override def get(key: K)(using Empty[V]): Maybe[V] =
      val hash = evalHash(key)

      @tailrec
      def loop(current: Tree, prevPos: Int): Maybe[V] =
        current match
          case branch: Branch =>
            val pos = index(hash, prevPos)
            val child = branch.childs(pos)
            if child eq null
            then Maybe.absent[V]
            else loop(child, pos)

          case leaf: Leaf =>
            @tailrec
            def traverse(cursor: Leaf): Maybe[V] =
              if cursor.key == key
              then Maybe.present(cursor.value)
              else if cursor.next eq null
              then Maybe.absent[V]
              else traverse(cursor.next)
            end traverse

            traverse(leaf)
      end loop

      loop(root, 0)
    end get

    override def contains(key: K)(using Empty[V]): Boolean =
      get(key).nonEmpty

    override def update(key: K, value: V)(using Empty[V]): Maybe[V] =
      val hash = evalHash(key)

      @tailrec
      def loop(parent: Branch, current: Tree, prevPos: Int): Maybe[V] =
        current match
          case branch: Branch =>
            val pos = index(hash, 0)
            val child = branch.childs(pos)
            if child eq null then
              branch.insert(key, value, pos)
              Maybe.absent[V]
            else loop(branch, current, pos)

          case leaf: Leaf =>
            val self = evalHash(leaf.key)
            if self == hash then
              @tailrec
              def traverse(cursor: Leaf): Maybe[V] =
                if cursor.key == key then
                  val old = cursor.value
                  cursor.value = value
                  Maybe.present(old)
                else if cursor.next eq null then
                  cursor.next = Leaf(key, value, null)
                  Maybe.absent[V]
                else traverse(cursor.next)
              end traverse

              traverse(leaf)
            else
              val growTo = Branch()
              parent.childs(prevPos) = growTo
              val newPos = index(self, prevPos)
              growTo.childs(newPos) = leaf
              val insert = index(hash, prevPos)
              growTo.insert(key, value, insert)
              Maybe.absent[V]
            end if
        end match
      end loop

      loop(
        null.asInstanceOf[Branch],
        root,
        0,
      )
    end update
    override def remove(key: K)(using Empty[V]): Maybe[V] =
      val hash = evalHash(key)

      @tailrec
      def loop(grandparent: Branch, parent: Branch, current: Tree, prevPos: Int, currPos: Int): Maybe[V] =
        current match
          case branch: Branch =>
            val nextPos = index(hash, currPos)
            val child = branch.childs(nextPos)
            if child eq null
            then Maybe.absent[V]
            else loop(parent, branch, child, prevPos, currPos)

          case leaf: Leaf =>
            if leaf.key == key then
              val next = leaf.next
              parent.childs(currPos) = next
              if next eq null then
                parent.load -= 1
                if parent.load <= 1 && (grandparent ne null)
                then grandparent.childs(prevPos) = parent.childs(currPos)
              end if
              Maybe.absent[V]

            else
              @tailrec
              def traverse(prev: Leaf, current: Leaf): Maybe[V] =
                if current eq null
                then Maybe.absent[V]
                else if current.key == key then
                  val old = current.value
                  prev.next = current.next
                  Maybe.present(old)
                else traverse(prev, current)
              end traverse

              traverse(leaf, leaf.next)
            end if
        end match
      end loop

      loop(
        null.asInstanceOf[Branch],
        null.asInstanceOf[Branch],
        root,
        0,
        0,
      )
    end remove

  end Impl

  def make[K, V](branchSize: Int = 16): ShrinkableMap[K, V] = new Impl(branchSize)

end ShrinkableMap

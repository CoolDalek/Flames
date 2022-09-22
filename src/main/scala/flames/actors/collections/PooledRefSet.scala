package flames.actors.collections

import flames.actors.ActorRef
import flames.actors.path.ActorPath

class PooledRefSet[T](
                       pool: ChunksPool[Array, AnyRef],
                       initSize: Int
                     ) extends ActorRefSet[T]:

  import HashUtil.*

  private type Ref = ActorRef[T]

  inline private def refArray() =
    pool.alloc().asInstanceOf[Array[Ref]]
  inline private def layerArray() =
    pool.alloc().asInstanceOf[Array[Layer]]
  inline private def free[R](used: Array[R]) =
    pool.dealloc(used.asInstanceOf[Array[AnyRef]])

  private var elements = 0

  private var root = Direct(
    array = refArray(),
    parent = null,
    1,
    0,
  )

  private sealed trait Layer {

    def add(ref: Ref, hash: Int, position: Int): Boolean

    def depth: Int

  }
  private class Direct(
                        array: Array[ActorRef[T]],
                        var parent: Indirect | Null,
                        val depth: Int,
                        self: Int,
                      ) extends Layer:
    private var stored = 0

    override def add(ref: Ref, hash: Int, position: Int): Boolean =
      val index = (position + hash) & (pool.chunkSize - 1)
      if(array(index) eq null)
        array(index) = ref
        tryGrow()
        true
      else false
    end add

    private def tryGrow(): Unit =
      stored += 1
      if(stored == pool.chunkSize - 1)
        val table = layerArray()
        val grown = Indirect(
          array = table,
          depth,
        )
        array.foreach { ref =>
          val hash = ref.##
          val position = hash * depth
          grown.add(ref, ref.##, position)
        }
        if(parent == null) parent = grown
        else parent.asInstanceOf[Indirect].array(self) = grown
    end tryGrow

  end Direct

  private class Indirect(
                          val array: Array[Layer],
                          val depth: Int,
                        ) extends Layer:

    override def add(ref: Ref, hash: Int, position: Int): Boolean =
      val updated = position + hash
      val index = updated & (pool.chunkSize - 1)
      val next = array(index)
      val addTo =
        if(next ne null) next
        else {
          val grown = Direct(
            refArray(),
            this,
            depth + 1,
            index,
          )
          array(index) = grown
          grown
        }
      addTo.add(
        ref,
        hash,
        updated,
      )
    end add

  end Indirect

  override def add(ref: ActorRef[T]): Boolean = ???

  override def remove(path: ActorPath): ActorRef[T] | Null = ???

  override def isEmpty: Boolean = ???

  override def nonEmpty: Boolean = ???

  override def foreach(consumer: ActorRef[T] => Unit): Unit = ???

  override def size: Int = ???

  override def length: Int = ???

object PooledRefSet:

end PooledRefSet

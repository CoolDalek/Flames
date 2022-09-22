package flames.actors.collections

import flames.actors.ActorRef
import flames.actors.path.ActorPath

class SimpleRefSet[T](initSize: Int, grows: Double) extends ActorRefSet[T] {
  private type Ref = ActorRef[T]
  private type Table = Array[Node]
  inline private def allocTable(size: Int) = new Array[Node](size)

  private class Node(val value: Ref, var next: Node | Null = null)

  private var table = allocTable(initSize)
  private var elements = 0

  override def size: Int = elements

  override def length: Int = elements

  private def tryGrow(): Boolean =
    if(elements == table.length)
      val bigger = allocTable((table.length * grows).toInt)
      foreach { ref =>
        val insert = index(ref, bigger)
        bigger(insert) = ref
      }
      table = bigger
      true
    else false
  end tryGrow

  inline private def index(ref: ActorRef[T], table: Table = table) = HashUtil.index(ref.path, table)
  inline private def index(path: ActorPath) = HashUtil.index(path, table)

  override def add(ref: ActorRef[T]): Boolean =
    val idx = index(ref)
    val bucket = table(idx)
    if(bucket eq null)
      elements += 1
      val insert = Node(ref)
      if (tryGrow()) {
        val position = index(ref)
        val node = table(position)
        if(node eq null)
          table(position) = insert
        else
          var cursor = node
          while cursor.next != null do cursor = cursor.next.asInstanceOf[Node]
          cursor.next = insert
      } else table(idx) = insert
      true
    else
      var cursor = bucket
      var present = false
      while
        present = cursor.value == ref
        cursor.next != null && !present
      do cursor = cursor.next.asInstanceOf[Node]
      if(!present)
        elements += 1
        val insert = Node(ref)
        tryGrow()
        true
      else false
  end add

  override def remove(path: ActorPath): ActorRef[T] | Null =
    val idx = index(path)
    val stored = table(idx)
    table(idx) = null
    stored
  end remove

  override def isEmpty: Boolean = elements == 0

  override def nonEmpty: Boolean = elements != 0

  inline override def foreach(consumer: ActorRef[T] => Unit): Unit =
    var i = 0
    while (i < table.length)
      val stored = table(i)
      if (stored ne null) consumer(stored)
      i += 1
  end foreach

}

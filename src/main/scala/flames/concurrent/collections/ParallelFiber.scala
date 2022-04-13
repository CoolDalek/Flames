package flames.concurrent.collections

import java.util.concurrent.atomic.AtomicInteger
import scala.collection.mutable
import scala.concurrent.ExecutionContext
import Parallel.*

import scala.collection.mutable.ArrayBuffer

class ParallelFiber(
                     parallel: ErasedParallel,
                     callback: ErasedCallback,
                     splitted: Splitted,
                   )(using ec: ExecutionContext) extends Eraser { self =>

  private var loop = true
  private var current = parallel
  private val stack = mutable.Stack.empty[Continuation]
  private val signalTo = AtomicInteger(0)

  private def processValue(splitted: Splitted): Unit =
    if (stack.isEmpty) {
      loop = false
      callback(splitted.collect())
    } else {
      stack.pop()(splitted)
    }

  def run(): Unit =
    while(loop) {
      current match {
        case Noop =>
          processValue(splitted)
        case Value(splitted) =>
          processValue(splitted)
        case Map(prev, action) =>
          stack.push { splitted =>
            executeAsyncAll(
              splitted,
              _.map(action.asInstanceOf[Any => Any])
            )
          }
          current = prev
        case FlatMap(prev, action, splitter) =>
          stack.push { splitted =>/*
            val flatMap = action.asInstanceOf[Any => ErasedCollection]
            val erased = eraseSplitter(splitter)
            val result = for {
              part <- splitted
              element <- part
              collection = flatMap(element)
            } yield erased.iterator(collection)
            Value(result)*/???
          }
          current = prev
      }
    }

  def executeAsyncAll(splitted: Splitted, function: UnsafeIterator[Any] => UnsafeIterator[Any]): Unit =
    if (splitted.hasNext) {
      loop = false
      val head = splitted.next()
      val task = Task(
        head,
        splitted,
        function,
      )
      splitted.forceSplit()
      task.all = splitted.knownSize
      splitted.forkCache.foreach { part =>
        val fork = Task(
          part,
          splitted,
          function,
        )
        fork.all = splitted.knownSize
        ec.execute(fork)
      }
      task.runInPlace()
    } else {
      current = Value(splitted)
    }

  class Task(
              part: UnsafeIterator[Any],
              splitted: Splitted,
              function: UnsafeIterator[Any] => UnsafeIterator[Any],
            ) extends Runnable {
    var all: Int = 0

    private def run(fork: Boolean): Unit = {
      function(part)
      if(signalTo.incrementAndGet() == all) {
        signalTo.set(0)
        loop = true
        splitted.reset()
        current = Value(splitted)
        if(fork) self.run()
      }
    }

    def runInPlace(): Unit =
      run(false)

    override def run(): Unit =
      run(true)

  }

}
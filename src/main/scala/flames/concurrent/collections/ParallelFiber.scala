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
                   )(using ec: ExecutionContext) extends Eraser:
  self =>

  private var loop = true
  private var current = parallel
  private val stack = mutable.Stack.empty[Continuation]
  private val signalTo = AtomicInteger(0)
  private var taskCount: Int = 0

  private def processValue(splitted: Splitted): Unit =
    if (stack.isEmpty) {
      loop = false
      callback(splitted.asIterator())
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
          stack.push { splitted =>
            executeAsyncAll(
              splitted,
              _.flatMap { elem =>
                val coll = action.asInstanceOf[Any => ErasedCollection](elem)
                eraseSplitter(splitter).split(coll)
              }
            )
          }
          current = prev
      }
    }
  end run

  def executeAsyncAll(splitted: Splitted, function: UnsafeIterator[Any] => UnsafeIterator[Any]): Unit =
    splitted.head match
      case null =>
        current = Value(splitted)
      case head =>
        loop = false
        val task = Task(
          head,
          splitted,
          function,
        )
        taskCount += splitted.size
        splitted.tail.foreach { part =>
          val fork = Task(
            part,
            splitted,
            function,
          )
          ec.execute(fork)
        }
        task.runInPlace()
  end executeAsyncAll

  class Task(
              part: UnsafeIterator[Any],
              splitted: Splitted,
              function: UnsafeIterator[Any] => UnsafeIterator[Any],
            ) extends Runnable:

    private def run(fork: Boolean): Unit = {
      function(part)
      if(signalTo.incrementAndGet() == taskCount) {
        loop = true
        current = Value(splitted)
        if(fork) self.run()
      }
    }

    def runInPlace(): Unit =
      run(false)

    override def run(): Unit =
      run(true)

  end Task

end ParallelFiber
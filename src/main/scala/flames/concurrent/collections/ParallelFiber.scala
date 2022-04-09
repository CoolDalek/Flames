package flames.concurrent.collections

import scala.collection.mutable

import scala.concurrent.ExecutionContext
import Parallel.*

class ParallelFiber(
                     parallel: ErasedParallel,
                     callback: ErasedCallback,
                     splitter: ErasedSplitter,
                     collection: ErasedCollection,
                   )(using ec: ExecutionContext) extends Eraser {

  private var loop = true
  private var current = parallel
  private val stack = mutable.Stack.empty[Continuation]

  inline private def processValue(splitted: Splitted): Unit =
    if (stack.isEmpty) {
      loop = false
      callback(splitted.flatten)
    } else {
      val cont = stack.pop()
      current = cont(splitted)
    }

  def run(): Unit =
    while(loop) {
      current match {
        case Noop =>
          val splitted = splitter.split(collection)
          processValue(splitted)
        case Value(splitted) =>
          val erased = eraseSplitted(splitted)
          processValue(erased)
        case Map(prev, action) =>
          stack.push { splitted =>
            val map = action.asInstanceOf[Any => Any]
            val result = splitted.map(_.map(map))
            Value(result)
          }
          current = prev
        case FlatMap(prev, action, splitter) =>
          stack.push { splitted =>
            val flatMap = action.asInstanceOf[Any => ErasedCollection]
            val erased = eraseSplitter(splitter)
            val result = for {
              part <- splitted
              element <- part
              collection = flatMap(element)
            } yield erased.iterator(collection)
            Value(result)
          }
          current = prev
      }
    }

}
package flames.concurrent.collections

import scala.concurrent.ExecutionContext
import scala.collection.mutable

import Parallel.*

sealed trait Parallel[+T] { self =>

  def map[R](action: T => R): Parallel[R] =
    Map(this, action)

  def flatMap[R, C[_]: Splitter](action: T => C[R]): Parallel[R] =
    FlatMap(this, action, Splitter[C])

  def run(callback: Iterator[T] => Unit)(using ec: ExecutionContext): Unit = {
    val stack = mutable.Stack.empty[Cont]
    var current = erasePar(self)
    var loop = true
    while(loop) {
      current match {
        case Apply(collection, splitter) =>
          if(stack.isEmpty) {
            loop = false
            val erasedCall = eraseCall(callback)
            val erasedSplit = eraseSplit(splitter)
            val erasedColl = eraseColl(collection)
            val result = erasedSplit.iterator(erasedColl)
            erasedCall(result)
          } else {
            val cont = stack.pop()
            current = cont(splitter.split(collection))
          }
        case Continue(splitted) =>
          if(stack.isEmpty) {
            loop = false
            val erased = eraseCall(callback)
            val collected = splitted.flatten
            erased(collected)
          } else {
            val cont = stack.pop()
            current = cont(splitted)
          }
        case FlatMap(prev, action, splitter) =>
          stack.push { splitted =>
            val flatMap = action.asInstanceOf[Any => ErasedCollection]
            val erased = eraseSplit(splitter)
            val result = splitted.flatMap { part =>
              part.flatMap { element =>
                val collection = flatMap(element)
                erased.iterator(collection)
              }
            }
            Apply(result, erased)
          }
          current = prev
        case Map(prev, action) =>
          stack.push { splitted =>
            val map = action.asInstanceOf[Any => Any]
            val result = splitted.map(_.map(map))
            Continue(result)
          }
          current = prev
      }
    }
  }

}
object Parallel {
  private type Splitted = Iterator[Iterator[Any]]
  private type ErasedParallel = Parallel[Any]
  private type Cont = Splitted => ErasedParallel
  private type ErasedCallback = Iterator[Any] => Unit
  private type Fix[T] = Any
  private type ErasedCollection = Fix[Any]
  private type ErasedSplitter = Splitter[Fix]
  inline private def eraseCall[T](callback: Iterator[T] => Unit): ErasedCallback = callback.asInstanceOf[ErasedCallback]
  inline private def erasePar[T](parallel: Parallel[T]): ErasedParallel = parallel.asInstanceOf[ErasedParallel]
  inline private def eraseColl[C[_], T](collection: C[T]): ErasedCollection = collection.asInstanceOf[ErasedCollection]
  inline private def eraseSplit[C[_]](splitter: Splitter[C]): ErasedSplitter = splitter.asInstanceOf[ErasedSplitter]

  private case class Apply[T, C[_]](collection: C[T], splitter: Splitter[C]) extends Parallel[T]
  private case class Map[T, R](prev: Parallel[T], action: T => R) extends Parallel[R]
  private case class FlatMap[T, R, C[_]](prev: Parallel[T], action: T => C[R], splitter: Splitter[C]) extends Parallel[R]
  private case class Continue(splitted: Splitted) extends Parallel[Any]

  inline def apply[T, C[_]: Splitter](collection: C[T]): Parallel[T] =
    Apply(collection, Splitter[C])

}
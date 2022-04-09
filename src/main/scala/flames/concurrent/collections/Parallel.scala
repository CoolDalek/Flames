package flames.concurrent.collections

import scala.concurrent.ExecutionContext
import scala.collection.mutable

import Parallel.*

sealed trait Parallel[-In, +Out] extends Eraser { self =>

  def map[Out2](action: Out => Out2): Parallel[In, Out2] =
    Map(this, action)

  def flatMap[Out2, C[_]: Splitter](action: Out => C[Out2]): Parallel[In, Out2] =
    FlatMap(this, action, Splitter[C])

  def run[C[+_]: Splitter](on: C[In])(callback: Iterator[Out] => Unit)(using ExecutionContext): Unit =
    new ParallelFiber(
      this,
      eraseCallback(callback),
      eraseSplitter(Splitter[C]),
      eraseCollection(on),
    ).run()

}
object Parallel {

  private[collections] case class Value[T](
                                            splitted: Iterator[Iterator[T]],
                                          ) extends Parallel[Any, T]

  private[collections] case class Map[In, Out1, Out2](
                                                       prev: Parallel[In, Out1],
                                                       action: Out1 => Out2,
                                                     ) extends Parallel[In, Out2]

  private[collections] case class FlatMap[In, Out1, Out2, C[_]](
                                                                 prev: Parallel[In, Out1],
                                                                 action: Out1 => C[Out2],
                                                                 splitter: Splitter[C],
                                                               ) extends Parallel[In, Out2]

  private[collections] case object Noop extends Parallel[Any, Nothing]

  def apply[T]: Parallel[T, T] = Noop

}
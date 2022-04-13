package flames.concurrent.collections

import scala.concurrent.ExecutionContext

object ParallelTest {

  @main def test(): Unit = {
    given ExecutionContext = ExecutionContext.global
    Parallel[Int].map { i =>
      println(Thread.currentThread)
      i + 1
    }.run(IArray.tabulate(20)(identity))(_.foreach(println))
    Thread.sleep(1000)
  }

}
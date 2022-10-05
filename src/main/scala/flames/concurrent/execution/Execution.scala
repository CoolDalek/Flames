package flames.concurrent.execution

trait Execution:

  def `yield`(): Unit
  
  def suspend(): Unit
  
  def continue(): Unit

object Execution:

  trait Continuation:

    private[Execution] def resume(): Unit

    private[Execution] def pause(): Unit

  end Continuation
  
end Execution
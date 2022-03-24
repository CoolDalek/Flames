package flames.concurrent

trait Cancellable {
  
  def isCancelled: Boolean
  
  def cancel(): Boolean

}
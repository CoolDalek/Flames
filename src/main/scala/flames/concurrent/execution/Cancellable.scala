package flames.concurrent.execution

trait Cancellable {

  def isCancelled: Boolean

  def cancel(): Boolean

}
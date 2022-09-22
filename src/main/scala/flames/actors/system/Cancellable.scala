package flames.actors.system

trait Cancellable {

  def isCancelled: Boolean

  def cancel(): Boolean

}

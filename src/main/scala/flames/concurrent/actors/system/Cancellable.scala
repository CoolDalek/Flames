package flames.concurrent.actors.system

trait Cancellable {

  def isCancelled: Boolean

  def cancel(): Boolean

}
object Cancellable {

  class Signal(trigger: () => Unit) extends Cancellable {
    
    @volatile
    private var flag = false
    
    override def isCancelled: Boolean = flag

    override def cancel(): Boolean =
      trigger(); true
    
    def cancelled(): Unit = flag = true
    
  }
  
  def signal(trigger: => Unit): Signal = Signal(() => trigger)

}
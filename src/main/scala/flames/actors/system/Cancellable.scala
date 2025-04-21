package flames.actors.system

import java.util.concurrent.Future as JFuture

trait Cancellable {

  def isCancelled: Boolean

  def cancel(): Boolean

}
object Cancellable {

  class Signal(trigger: () => Unit) extends Cancellable {

    @volatile
    private var flag = false

    override def isCancelled: Boolean = flag

    // send cancellation signal
    override def cancel(): Boolean =
      trigger()
      true

    // confirm that cancellation happened
    def cancelled(): Unit = flag = true

  }

  def signal(trigger: => Unit): Signal = Signal(() => trigger)

  def javaFuture[T](future: JFuture[T], interrupt: Boolean): Cancellable = new Cancellable {
    override def isCancelled: Boolean = future.isCancelled
    override def cancel(): Boolean = future.cancel(interrupt)
  }

}
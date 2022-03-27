package flames.concurrent

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicReference
import scala.util.control.NonFatal
import Behavior.*

trait Worker[T](using val runtime: WorkersRuntime) {

  private val executor = runtime.makeExecutor(act())

  final def tell(message: T): Unit = executor.tell(message)

  inline protected final def receive(inline receiver: T => Behavior[T]): Behavior[T] = Receive(receiver)

  inline protected final def stop: Behavior[T] = Stop

  inline protected final def same: Behavior[T] = Pass

  inline protected final def self: Worker[T] = this

  def act(): Behavior[T]

}
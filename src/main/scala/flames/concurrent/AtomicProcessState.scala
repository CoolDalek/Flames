package flames.concurrent

import java.util.concurrent.atomic.AtomicInteger

opaque type AtomicProcessState = AtomicInteger
object AtomicProcessState {

  inline def apply(initial: ProcessState): AtomicProcessState = AtomicInteger(initial)

  extension (self: AtomicProcessState) {

    inline def compareAndSet(expected: ProcessState, newValue: ProcessState): Boolean =
      self.compareAndSet(expected, newValue)

    inline def set(value: ProcessState): Unit =
      self.set(value)

    inline def get(): ProcessState =
      self.get().asInstanceOf[ProcessState]

  }

}
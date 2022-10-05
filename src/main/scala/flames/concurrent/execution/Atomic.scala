package flames.concurrent.execution

trait Atomic[T]:

  def getPlain: T

  def getOpaque: T

  def getAcquire: T

  def get: T

  def setPlain(value: T): Unit

  def setOpaque(value: T): Unit

  def setRelease(value: T): Unit

  def set(value: T): Unit

  def getAndSetAcquire(value: T): T

  def getAndSetRelease(value: T): T

  def getAndSet(value: T): T

  def compareAndSet(expected: T, exchanged: T): Boolean

  def compareAndExchangeAcquire(expected: T, exchanged: T): T

  def compareAndExchangeRelease(expected: T, exchanged: T): T

  def compareAndExchange(expected: T, exchanged: T): T

  def weakCompareAndSetPlain(expected: T, exchanged: T): Boolean

  def weakCompareAndSetAcquire(expected: T, exchanged: T): Boolean

  def weakCompareAndSetRelease(expected: T, exchanged: T): Boolean

  def weakCompareAndSet(expected: T, exchanged: T): Boolean

end Atomic

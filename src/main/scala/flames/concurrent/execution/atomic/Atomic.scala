package flames.concurrent.execution.atomic

sealed trait AtomicSet[-T]:

  def setPlain(value: T): Unit

  def setOpaque(value: T): Unit

  def setRelease(value: T): Unit

  def set(value: T): Unit

end AtomicSet

sealed trait AtomicGet[+T]:

  def getPlain: T

  def getOpaque: T

  def getAcquire: T

  def get: T

end AtomicGet

sealed trait Atomic[T] extends AtomicGet[T], AtomicSet[T]:

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

trait AtomicRef[T <: AnyRef] extends Atomic[T]

sealed trait AtomicVal[T <: AnyVal] extends Atomic[T]
trait AtomicBool extends AtomicVal[Boolean]

sealed trait AtomicNumber[T] extends Atomic[T]

trait AtomicIntegral[T] extends AtomicNumber[T]
trait AtomicByte extends AtomicIntegral[Byte], AtomicVal[Byte]
trait AtomicShort extends AtomicIntegral[Short], AtomicVal[Short]
trait AtomicInt extends AtomicIntegral[Int], AtomicVal[Int]
trait AtomicLong extends AtomicIntegral[Long], AtomicVal[Long]

trait AtomicFractional[T] extends AtomicNumber[T]
trait AtomicFloat extends AtomicFractional[Float], AtomicVal[Float]
trait AtomicDouble extends AtomicFractional[Double], AtomicVal[Double]

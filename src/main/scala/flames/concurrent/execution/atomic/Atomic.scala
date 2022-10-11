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

sealed trait AtomicNumber[T] extends Atomic[T]:

  def getAndAdd(value: T): T

  def getAndAddAcquire(value: T): T

  def getAndAddRelease(value: T): T

end AtomicNumber

trait AtomicIntegral[T] extends AtomicNumber[T]:

  def getAndBitwiseOr(value: T): T

  def getAndBitwiseOrAcquire(value: T): T

  def getAndBitwiseOrRelease(value: T): T

  def getAndBitwiseAnd(value: T): T

  def getAndBitwiseAndAcquire(value: T): T

  def getAndBitwiseAndRelease(value: T): T

  def getAndBitwiseXor(value: T): T

  def getAndBitwiseXorAcquire(value: T): T

  def getAndBitwiseXorRelease(value: T): T

end AtomicIntegral

trait AtomicFractional[T] extends AtomicNumber[T]

trait AtomicBool extends AtomicVal[Boolean]:
  override def setPlain(value: Boolean): Unit
  override def setOpaque(value: Boolean): Unit
  override def setRelease(value: Boolean): Unit
  override def set(value: Boolean): Unit
  override def getPlain: Boolean
  override def getOpaque: Boolean
  override def getAcquire: Boolean
  override def get: Boolean
  override def getAndSetAcquire(value: Boolean): Boolean
  override def getAndSetRelease(value: Boolean): Boolean
  override def getAndSet(value: Boolean): Boolean
  override def compareAndSet(expected: Boolean, exchanged: Boolean): Boolean
  override def compareAndExchangeAcquire(expected: Boolean, exchanged: Boolean): Boolean
  override def compareAndExchangeRelease(expected: Boolean, exchanged: Boolean): Boolean
  override def compareAndExchange(expected: Boolean, exchanged: Boolean): Boolean
  override def weakCompareAndSetPlain(expected: Boolean, exchanged: Boolean): Boolean
  override def weakCompareAndSetAcquire(expected: Boolean, exchanged: Boolean): Boolean
  override def weakCompareAndSetRelease(expected: Boolean, exchanged: Boolean): Boolean
  override def weakCompareAndSet(expected: Boolean, exchanged: Boolean): Boolean
end AtomicBool

trait AtomicByte extends AtomicIntegral[Byte], AtomicVal[Byte]:
  override def setPlain(value: Byte): Unit
  override def setOpaque(value: Byte): Unit
  override def setRelease(value: Byte): Unit
  override def set(value: Byte): Unit
  override def getPlain: Byte
  override def getOpaque: Byte
  override def getAcquire: Byte
  override def get: Byte
  override def getAndBitwiseOr(value: Byte): Byte
  override def getAndBitwiseOrAcquire(value: Byte): Byte
  override def getAndBitwiseOrRelease(value: Byte): Byte
  override def getAndBitwiseAnd(value: Byte): Byte
  override def getAndBitwiseAndAcquire(value: Byte): Byte
  override def getAndBitwiseAndRelease(value: Byte): Byte
  override def getAndBitwiseXor(value: Byte): Byte
  override def getAndBitwiseXorAcquire(value: Byte): Byte
  override def getAndBitwiseXorRelease(value: Byte): Byte
  override def getAndAdd(value: Byte): Byte
  override def getAndAddAcquire(value: Byte): Byte
  override def getAndAddRelease(value: Byte): Byte
  override def getAndSetAcquire(value: Byte): Byte
  override def getAndSetRelease(value: Byte): Byte
  override def getAndSet(value: Byte): Byte
  override def compareAndSet(expected: Byte, exchanged: Byte): Boolean
  override def compareAndExchangeAcquire(expected: Byte, exchanged: Byte): Byte
  override def compareAndExchangeRelease(expected: Byte, exchanged: Byte): Byte
  override def compareAndExchange(expected: Byte, exchanged: Byte): Byte
  override def weakCompareAndSetPlain(expected: Byte, exchanged: Byte): Boolean
  override def weakCompareAndSetAcquire(expected: Byte, exchanged: Byte): Boolean
  override def weakCompareAndSetRelease(expected: Byte, exchanged: Byte): Boolean
  override def weakCompareAndSet(expected: Byte, exchanged: Byte): Boolean
end AtomicByte

trait AtomicChar extends AtomicIntegral[Char], AtomicVal[Char]:
  override def setPlain(value: Char): Unit
  override def setOpaque(value: Char): Unit
  override def setRelease(value: Char): Unit
  override def set(value: Char): Unit
  override def getPlain: Char
  override def getOpaque: Char
  override def getAcquire: Char
  override def get: Char
  override def getAndBitwiseOr(value: Char): Char
  override def getAndBitwiseOrAcquire(value: Char): Char
  override def getAndBitwiseOrRelease(value: Char): Char
  override def getAndBitwiseAnd(value: Char): Char
  override def getAndBitwiseAndAcquire(value: Char): Char
  override def getAndBitwiseAndRelease(value: Char): Char
  override def getAndBitwiseXor(value: Char): Char
  override def getAndBitwiseXorAcquire(value: Char): Char
  override def getAndBitwiseXorRelease(value: Char): Char
  override def getAndAdd(value: Char): Char
  override def getAndAddAcquire(value: Char): Char
  override def getAndAddRelease(value: Char): Char
  override def getAndSetAcquire(value: Char): Char
  override def getAndSetRelease(value: Char): Char
  override def getAndSet(value: Char): Char
  override def compareAndSet(expected: Char, exchanged: Char): Boolean
  override def compareAndExchangeAcquire(expected: Char, exchanged: Char): Char
  override def compareAndExchangeRelease(expected: Char, exchanged: Char): Char
  override def compareAndExchange(expected: Char, exchanged: Char): Char
  override def weakCompareAndSetPlain(expected: Char, exchanged: Char): Boolean
  override def weakCompareAndSetAcquire(expected: Char, exchanged: Char): Boolean
  override def weakCompareAndSetRelease(expected: Char, exchanged: Char): Boolean
  override def weakCompareAndSet(expected: Char, exchanged: Char): Boolean
end AtomicChar

trait AtomicShort extends AtomicIntegral[Short], AtomicVal[Short]:
  override def setPlain(value: Short): Unit
  override def setOpaque(value: Short): Unit
  override def setRelease(value: Short): Unit
  override def set(value: Short): Unit
  override def getPlain: Short
  override def getOpaque: Short
  override def getAcquire: Short
  override def get: Short
  override def getAndBitwiseOr(value: Short): Short
  override def getAndBitwiseOrAcquire(value: Short): Short
  override def getAndBitwiseOrRelease(value: Short): Short
  override def getAndBitwiseAnd(value: Short): Short
  override def getAndBitwiseAndAcquire(value: Short): Short
  override def getAndBitwiseAndRelease(value: Short): Short
  override def getAndBitwiseXor(value: Short): Short
  override def getAndBitwiseXorAcquire(value: Short): Short
  override def getAndBitwiseXorRelease(value: Short): Short
  override def getAndAdd(value: Short): Short
  override def getAndAddAcquire(value: Short): Short
  override def getAndAddRelease(value: Short): Short
  override def getAndSetAcquire(value: Short): Short
  override def getAndSetRelease(value: Short): Short
  override def getAndSet(value: Short): Short
  override def compareAndSet(expected: Short, exchanged: Short): Boolean
  override def compareAndExchangeAcquire(expected: Short, exchanged: Short): Short
  override def compareAndExchangeRelease(expected: Short, exchanged: Short): Short
  override def compareAndExchange(expected: Short, exchanged: Short): Short
  override def weakCompareAndSetPlain(expected: Short, exchanged: Short): Boolean
  override def weakCompareAndSetAcquire(expected: Short, exchanged: Short): Boolean
  override def weakCompareAndSetRelease(expected: Short, exchanged: Short): Boolean
  override def weakCompareAndSet(expected: Short, exchanged: Short): Boolean
end AtomicShort

trait AtomicInt extends AtomicIntegral[Int], AtomicVal[Int]:
  override def setPlain(value: Int): Unit
  override def setOpaque(value: Int): Unit
  override def setRelease(value: Int): Unit
  override def set(value: Int): Unit
  override def getPlain: Int
  override def getOpaque: Int
  override def getAcquire: Int
  override def get: Int
  override def getAndBitwiseOr(value: Int): Int
  override def getAndBitwiseOrAcquire(value: Int): Int
  override def getAndBitwiseOrRelease(value: Int): Int
  override def getAndBitwiseAnd(value: Int): Int
  override def getAndBitwiseAndAcquire(value: Int): Int
  override def getAndBitwiseAndRelease(value: Int): Int
  override def getAndBitwiseXor(value: Int): Int
  override def getAndBitwiseXorAcquire(value: Int): Int
  override def getAndBitwiseXorRelease(value: Int): Int
  override def getAndAdd(value: Int): Int
  override def getAndAddAcquire(value: Int): Int
  override def getAndAddRelease(value: Int): Int
  override def getAndSetAcquire(value: Int): Int
  override def getAndSetRelease(value: Int): Int
  override def getAndSet(value: Int): Int
  override def compareAndSet(expected: Int, exchanged: Int): Boolean
  override def compareAndExchangeAcquire(expected: Int, exchanged: Int): Int
  override def compareAndExchangeRelease(expected: Int, exchanged: Int): Int
  override def compareAndExchange(expected: Int, exchanged: Int): Int
  override def weakCompareAndSetPlain(expected: Int, exchanged: Int): Boolean
  override def weakCompareAndSetAcquire(expected: Int, exchanged: Int): Boolean
  override def weakCompareAndSetRelease(expected: Int, exchanged: Int): Boolean
  override def weakCompareAndSet(expected: Int, exchanged: Int): Boolean
end AtomicInt

trait AtomicLong extends AtomicIntegral[Long], AtomicVal[Long]:
  override def setPlain(value: Long): Unit
  override def setOpaque(value: Long): Unit
  override def setRelease(value: Long): Unit
  override def set(value: Long): Unit
  override def getPlain: Long
  override def getOpaque: Long
  override def getAcquire: Long
  override def get: Long
  override def getAndBitwiseOr(value: Long): Long
  override def getAndBitwiseOrAcquire(value: Long): Long
  override def getAndBitwiseOrRelease(value: Long): Long
  override def getAndBitwiseAnd(value: Long): Long
  override def getAndBitwiseAndAcquire(value: Long): Long
  override def getAndBitwiseAndRelease(value: Long): Long
  override def getAndBitwiseXor(value: Long): Long
  override def getAndBitwiseXorAcquire(value: Long): Long
  override def getAndBitwiseXorRelease(value: Long): Long
  override def getAndAdd(value: Long): Long
  override def getAndAddAcquire(value: Long): Long
  override def getAndAddRelease(value: Long): Long
  override def getAndSetAcquire(value: Long): Long
  override def getAndSetRelease(value: Long): Long
  override def getAndSet(value: Long): Long
  override def compareAndSet(expected: Long, exchanged: Long): Boolean
  override def compareAndExchangeAcquire(expected: Long, exchanged: Long): Long
  override def compareAndExchangeRelease(expected: Long, exchanged: Long): Long
  override def compareAndExchange(expected: Long, exchanged: Long): Long
  override def weakCompareAndSetPlain(expected: Long, exchanged: Long): Boolean
  override def weakCompareAndSetAcquire(expected: Long, exchanged: Long): Boolean
  override def weakCompareAndSetRelease(expected: Long, exchanged: Long): Boolean
  override def weakCompareAndSet(expected: Long, exchanged: Long): Boolean
end AtomicLong

trait AtomicFloat extends AtomicFractional[Float], AtomicVal[Float]:
  override def setPlain(value: Float): Unit
  override def setOpaque(value: Float): Unit
  override def setRelease(value: Float): Unit
  override def set(value: Float): Unit
  override def getPlain: Float
  override def getOpaque: Float
  override def getAcquire: Float
  override def get: Float
  override def getAndAdd(value: Float): Float
  override def getAndAddAcquire(value: Float): Float
  override def getAndAddRelease(value: Float): Float
  override def getAndSetAcquire(value: Float): Float
  override def getAndSetRelease(value: Float): Float
  override def getAndSet(value: Float): Float
  override def compareAndSet(expected: Float, exchanged: Float): Boolean
  override def compareAndExchangeAcquire(expected: Float, exchanged: Float): Float
  override def compareAndExchangeRelease(expected: Float, exchanged: Float): Float
  override def compareAndExchange(expected: Float, exchanged: Float): Float
  override def weakCompareAndSetPlain(expected: Float, exchanged: Float): Boolean
  override def weakCompareAndSetAcquire(expected: Float, exchanged: Float): Boolean
  override def weakCompareAndSetRelease(expected: Float, exchanged: Float): Boolean
  override def weakCompareAndSet(expected: Float, exchanged: Float): Boolean
end AtomicFloat

trait AtomicDouble extends AtomicFractional[Double], AtomicVal[Double]:
  override def setPlain(value: Double): Unit
  override def setOpaque(value: Double): Unit
  override def setRelease(value: Double): Unit
  override def set(value: Double): Unit
  override def getPlain: Double
  override def getOpaque: Double
  override def getAcquire: Double
  override def get: Double
  override def getAndAdd(value: Double): Double
  override def getAndAddAcquire(value: Double): Double
  override def getAndAddRelease(value: Double): Double
  override def getAndSetAcquire(value: Double): Double
  override def getAndSetRelease(value: Double): Double
  override def getAndSet(value: Double): Double
  override def compareAndSet(expected: Double, exchanged: Double): Boolean
  override def compareAndExchangeAcquire(expected: Double, exchanged: Double): Double
  override def compareAndExchangeRelease(expected: Double, exchanged: Double): Double
  override def compareAndExchange(expected: Double, exchanged: Double): Double
  override def weakCompareAndSetPlain(expected: Double, exchanged: Double): Boolean
  override def weakCompareAndSetAcquire(expected: Double, exchanged: Double): Boolean
  override def weakCompareAndSetRelease(expected: Double, exchanged: Double): Boolean
  override def weakCompareAndSet(expected: Double, exchanged: Double): Boolean
end AtomicDouble

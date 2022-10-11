package flames.concurrent.execution.atomic

object SingleThread extends AtomicFactory {

  override def ref[T <: AnyRef](init: T): AtomicRef[T] = new:
    private var underlying: T = init
    override def getAndSetAcquire(value: T): T = getAndSet(value)
    override def getAndSetRelease(value: T): T = getAndSet(value)
    inline def getAndSet(value: T): T = {
      val prev = underlying
      underlying = value
      prev
    }
    override def compareAndSet(expected: T, exchanged: T): Boolean =
      if(underlying == expected)
        underlying = exchanged
        true
      else false
    override def compareAndExchangeAcquire(expected: T, exchanged: T): T = compareAndExchange(expected, exchanged)
    override def compareAndExchangeRelease(expected: T, exchanged: T): T = compareAndExchange(expected, exchanged)
    inline override def compareAndExchange(expected: T, exchanged: T): T =
      if(underlying == expected) {
        underlying = exchanged
        expected
      } else underlying
    override def weakCompareAndSetPlain(expected: T, exchanged: T): Boolean = compareAndSet(expected, exchanged)
    override def weakCompareAndSetAcquire(expected: T, exchanged: T): Boolean = compareAndSet(expected, exchanged)
    override def weakCompareAndSetRelease(expected: T, exchanged: T): Boolean = compareAndSet(expected, exchanged)
    override def weakCompareAndSet(expected: T, exchanged: T): Boolean = compareAndSet(expected, exchanged)
    override def setPlain(value: T): Unit = set(value)
    override def setOpaque(value: T): Unit = set(value)
    override def setRelease(value: T): Unit = set(value)
    inline override def set(value: T): Unit = underlying = value
    override def getPlain: T = get
    override def getOpaque: T = get
    override def getAcquire: T = get
    inline override def get: T = underlying
  end ref

  override def boolean(init: Boolean): AtomicBool = new:
    private var underlying: Boolean = init
    override def getAndSetAcquire(value: Boolean): Boolean = getAndSet(value)
    override def getAndSetRelease(value: Boolean): Boolean = getAndSet(value)
    inline def getAndSet(value: Boolean): Boolean = {
      val prev = underlying
      underlying = value
      prev
    }
    override def compareAndSet(expected: Boolean, exchanged: Boolean): Boolean =
      if(underlying == expected)
        underlying = exchanged
        true
      else false
    override def compareAndExchangeAcquire(expected: Boolean, exchanged: Boolean): Boolean = compareAndExchange(expected, exchanged)
    override def compareAndExchangeRelease(expected: Boolean, exchanged: Boolean): Boolean = compareAndExchange(expected, exchanged)
    inline override def compareAndExchange(expected: Boolean, exchanged: Boolean): Boolean =
      if(underlying == expected) {
        underlying = exchanged
        expected
      } else underlying
    override def weakCompareAndSetPlain(expected: Boolean, exchanged: Boolean): Boolean = compareAndSet(expected, exchanged)
    override def weakCompareAndSetAcquire(expected: Boolean, exchanged: Boolean): Boolean = compareAndSet(expected, exchanged)
    override def weakCompareAndSetRelease(expected: Boolean, exchanged: Boolean): Boolean = compareAndSet(expected, exchanged)
    override def weakCompareAndSet(expected: Boolean, exchanged: Boolean): Boolean = compareAndSet(expected, exchanged)
    override def setPlain(value: Boolean): Unit = set(value)
    override def setOpaque(value: Boolean): Unit = set(value)
    override def setRelease(value: Boolean): Unit = set(value)
    inline override def set(value: Boolean): Unit = underlying = value
    override def getPlain: Boolean = get
    override def getOpaque: Boolean = get
    override def getAcquire: Boolean = get
    inline override def get: Boolean = underlying
  end boolean

  override def byte(init: Byte): AtomicByte = new:
    private var underlying: Byte = init
    override def getAndSetAcquire(value: Byte): Byte = getAndSet(value)
    override def getAndSetRelease(value: Byte): Byte = getAndSet(value)
    inline def getAndSet(value: Byte): Byte = {
      val prev = underlying
      underlying = value
      prev
    }
    override def compareAndSet(expected: Byte, exchanged: Byte): Boolean =
      if(underlying == expected)
        underlying = exchanged
        true
      else false
    override def compareAndExchangeAcquire(expected: Byte, exchanged: Byte): Byte = compareAndExchange(expected, exchanged)
    override def compareAndExchangeRelease(expected: Byte, exchanged: Byte): Byte = compareAndExchange(expected, exchanged)
    inline override def compareAndExchange(expected: Byte, exchanged: Byte): Byte =
      if(underlying == expected) {
        underlying = exchanged
        expected
      } else underlying
    override def weakCompareAndSetPlain(expected: Byte, exchanged: Byte): Boolean = compareAndSet(expected, exchanged)
    override def weakCompareAndSetAcquire(expected: Byte, exchanged: Byte): Boolean = compareAndSet(expected, exchanged)
    override def weakCompareAndSetRelease(expected: Byte, exchanged: Byte): Boolean = compareAndSet(expected, exchanged)
    override def weakCompareAndSet(expected: Byte, exchanged: Byte): Boolean = compareAndSet(expected, exchanged)
    override def setPlain(value: Byte): Unit = set(value)
    override def setOpaque(value: Byte): Unit = set(value)
    override def setRelease(value: Byte): Unit = set(value)
    inline override def set(value: Byte): Unit = underlying = value
    override def getPlain: Byte = get
    override def getOpaque: Byte = get
    override def getAcquire: Byte = get
    inline override def get: Byte = underlying
    inline override def getAndBitwiseOr(value: Byte): Byte = {
      val prev = underlying
      underlying = (underlying | value).toByte
      prev
    }
    override def getAndBitwiseOrAcquire(value: Byte): Byte = getAndBitwiseOr(value)
    override def getAndBitwiseOrRelease(value: Byte): Byte = getAndBitwiseOr(value)
    inline override def getAndBitwiseAnd(value: Byte): Byte = {
      val prev = underlying
      underlying = (underlying & value).toByte
      prev
    }
    override def getAndBitwiseAndAcquire(value: Byte): Byte = getAndBitwiseAnd(value)
    override def getAndBitwiseAndRelease(value: Byte): Byte = getAndBitwiseAnd(value)
    inline override def getAndBitwiseXor(value: Byte): Byte = {
      val prev = underlying
      underlying = (underlying & value).toByte
      prev
    }
    override def getAndBitwiseXorAcquire(value: Byte): Byte = getAndBitwiseXor(value)
    override def getAndBitwiseXorRelease(value: Byte): Byte = getAndBitwiseXor(value)
    inline override def getAndAdd(value: Byte): Byte = {
      val prev = underlying
      underlying = (underlying + value).toByte
      prev
    }
    override def getAndAddAcquire(value: Byte): Byte = getAndAdd(value)
    override def getAndAddRelease(value: Byte): Byte = getAndAdd(value)
  end byte

  override def char(init: Char): AtomicChar = new:
    private var underlying: Char = init
    override def getAndSetAcquire(value: Char): Char = getAndSet(value)
    override def getAndSetRelease(value: Char): Char = getAndSet(value)
    inline def getAndSet(value: Char): Char = {
      val prev = underlying
      underlying = value
      prev
    }
    override def compareAndSet(expected: Char, exchanged: Char): Boolean =
      if(underlying == expected)
        underlying = exchanged
        true
      else false
    override def compareAndExchangeAcquire(expected: Char, exchanged: Char): Char = compareAndExchange(expected, exchanged)
    override def compareAndExchangeRelease(expected: Char, exchanged: Char): Char = compareAndExchange(expected, exchanged)
    inline override def compareAndExchange(expected: Char, exchanged: Char): Char =
      if(underlying == expected) {
        underlying = exchanged
        expected
      } else underlying
    override def weakCompareAndSetPlain(expected: Char, exchanged: Char): Boolean = compareAndSet(expected, exchanged)
    override def weakCompareAndSetAcquire(expected: Char, exchanged: Char): Boolean = compareAndSet(expected, exchanged)
    override def weakCompareAndSetRelease(expected: Char, exchanged: Char): Boolean = compareAndSet(expected, exchanged)
    override def weakCompareAndSet(expected: Char, exchanged: Char): Boolean = compareAndSet(expected, exchanged)
    override def setPlain(value: Char): Unit = set(value)
    override def setOpaque(value: Char): Unit = set(value)
    override def setRelease(value: Char): Unit = set(value)
    inline override def set(value: Char): Unit = underlying = value
    override def getPlain: Char = get
    override def getOpaque: Char = get
    override def getAcquire: Char = get
    inline override def get: Char = underlying
    inline override def getAndBitwiseOr(value: Char): Char = {
      val prev = underlying
      underlying = (underlying | value).toChar
      prev
    }
    override def getAndBitwiseOrAcquire(value: Char): Char = getAndBitwiseOr(value)
    override def getAndBitwiseOrRelease(value: Char): Char = getAndBitwiseOr(value)
    inline override def getAndBitwiseAnd(value: Char): Char = {
      val prev = underlying
      underlying = (underlying & value).toChar
      prev
    }
    override def getAndBitwiseAndAcquire(value: Char): Char = getAndBitwiseAnd(value)
    override def getAndBitwiseAndRelease(value: Char): Char = getAndBitwiseAnd(value)
    inline override def getAndBitwiseXor(value: Char): Char = {
      val prev = underlying
      underlying = (underlying & value).toChar
      prev
    }
    override def getAndBitwiseXorAcquire(value: Char): Char = getAndBitwiseXor(value)
    override def getAndBitwiseXorRelease(value: Char): Char = getAndBitwiseXor(value)
    inline override def getAndAdd(value: Char): Char = {
      val prev = underlying
      underlying = (underlying + value).toChar
      prev
    }
    override def getAndAddAcquire(value: Char): Char = getAndAdd(value)
    override def getAndAddRelease(value: Char): Char = getAndAdd(value)
  end char

  override def short(init: Short): AtomicShort = new:
    private var underlying: Short = init
    override def getAndSetAcquire(value: Short): Short = getAndSet(value)
    override def getAndSetRelease(value: Short): Short = getAndSet(value)
    inline def getAndSet(value: Short): Short = {
      val prev = underlying
      underlying = value
      prev
    }
    override def compareAndSet(expected: Short, exchanged: Short): Boolean =
      if(underlying == expected)
        underlying = exchanged
        true
      else false
    override def compareAndExchangeAcquire(expected: Short, exchanged: Short): Short = compareAndExchange(expected, exchanged)
    override def compareAndExchangeRelease(expected: Short, exchanged: Short): Short = compareAndExchange(expected, exchanged)
    inline override def compareAndExchange(expected: Short, exchanged: Short): Short =
      if(underlying == expected) {
        underlying = exchanged
        expected
      } else underlying
    override def weakCompareAndSetPlain(expected: Short, exchanged: Short): Boolean = compareAndSet(expected, exchanged)
    override def weakCompareAndSetAcquire(expected: Short, exchanged: Short): Boolean = compareAndSet(expected, exchanged)
    override def weakCompareAndSetRelease(expected: Short, exchanged: Short): Boolean = compareAndSet(expected, exchanged)
    override def weakCompareAndSet(expected: Short, exchanged: Short): Boolean = compareAndSet(expected, exchanged)
    override def setPlain(value: Short): Unit = set(value)
    override def setOpaque(value: Short): Unit = set(value)
    override def setRelease(value: Short): Unit = set(value)
    inline override def set(value: Short): Unit = underlying = value
    override def getPlain: Short = get
    override def getOpaque: Short = get
    override def getAcquire: Short = get
    inline override def get: Short = underlying
    inline override def getAndBitwiseOr(value: Short): Short = {
      val prev = underlying
      underlying = (underlying | value).toShort
      prev
    }
    override def getAndBitwiseOrAcquire(value: Short): Short = getAndBitwiseOr(value)
    override def getAndBitwiseOrRelease(value: Short): Short = getAndBitwiseOr(value)
    inline override def getAndBitwiseAnd(value: Short): Short = {
      val prev = underlying
      underlying = (underlying & value).toShort
      prev
    }
    override def getAndBitwiseAndAcquire(value: Short): Short = getAndBitwiseAnd(value)
    override def getAndBitwiseAndRelease(value: Short): Short = getAndBitwiseAnd(value)
    inline override def getAndBitwiseXor(value: Short): Short = {
      val prev = underlying
      underlying = (underlying & value).toShort
      prev
    }
    override def getAndBitwiseXorAcquire(value: Short): Short = getAndBitwiseXor(value)
    override def getAndBitwiseXorRelease(value: Short): Short = getAndBitwiseXor(value)
    inline override def getAndAdd(value: Short): Short = {
      val prev = underlying
      underlying = (underlying + value).toShort
      prev
    }
    override def getAndAddAcquire(value: Short): Short = getAndAdd(value)
    override def getAndAddRelease(value: Short): Short = getAndAdd(value)
  end short

  override def int(init: Int): AtomicInt = new:
    private var underlying: Int = init
    override def getAndSetAcquire(value: Int): Int = getAndSet(value)
    override def getAndSetRelease(value: Int): Int = getAndSet(value)
    inline def getAndSet(value: Int): Int = {
      val prev = underlying
      underlying = value
      prev
    }
    override def compareAndSet(expected: Int, exchanged: Int): Boolean =
      if(underlying == expected)
        underlying = exchanged
        true
      else false
    override def compareAndExchangeAcquire(expected: Int, exchanged: Int): Int = compareAndExchange(expected, exchanged)
    override def compareAndExchangeRelease(expected: Int, exchanged: Int): Int = compareAndExchange(expected, exchanged)
    inline override def compareAndExchange(expected: Int, exchanged: Int): Int =
      if(underlying == expected) {
        underlying = exchanged
        expected
      } else underlying
    override def weakCompareAndSetPlain(expected: Int, exchanged: Int): Boolean = compareAndSet(expected, exchanged)
    override def weakCompareAndSetAcquire(expected: Int, exchanged: Int): Boolean = compareAndSet(expected, exchanged)
    override def weakCompareAndSetRelease(expected: Int, exchanged: Int): Boolean = compareAndSet(expected, exchanged)
    override def weakCompareAndSet(expected: Int, exchanged: Int): Boolean = compareAndSet(expected, exchanged)
    override def setPlain(value: Int): Unit = set(value)
    override def setOpaque(value: Int): Unit = set(value)
    override def setRelease(value: Int): Unit = set(value)
    inline override def set(value: Int): Unit = underlying = value
    override def getPlain: Int = get
    override def getOpaque: Int = get
    override def getAcquire: Int = get
    inline override def get: Int = underlying
    inline override def getAndBitwiseOr(value: Int): Int = {
      val prev = underlying
      underlying = underlying | value
      prev
    }
    override def getAndBitwiseOrAcquire(value: Int): Int = getAndBitwiseOr(value)
    override def getAndBitwiseOrRelease(value: Int): Int = getAndBitwiseOr(value)
    inline override def getAndBitwiseAnd(value: Int): Int = {
      val prev = underlying
      underlying = underlying & value
      prev
    }
    override def getAndBitwiseAndAcquire(value: Int): Int = getAndBitwiseAnd(value)
    override def getAndBitwiseAndRelease(value: Int): Int = getAndBitwiseAnd(value)
    inline override def getAndBitwiseXor(value: Int): Int = {
      val prev = underlying
      underlying = underlying & value
      prev
    }
    override def getAndBitwiseXorAcquire(value: Int): Int = getAndBitwiseXor(value)
    override def getAndBitwiseXorRelease(value: Int): Int = getAndBitwiseXor(value)
    inline override def getAndAdd(value: Int): Int = {
      val prev = underlying
      underlying = underlying + value
      prev
    }
    override def getAndAddAcquire(value: Int): Int = getAndAdd(value)
    override def getAndAddRelease(value: Int): Int = getAndAdd(value)
  end int

  override def long(init: Long): AtomicLong = new:
    private var underlying: Long = init
    override def getAndSetAcquire(value: Long): Long = getAndSet(value)
    override def getAndSetRelease(value: Long): Long = getAndSet(value)
    inline def getAndSet(value: Long): Long = {
      val prev = underlying
      underlying = value
      prev
    }
    override def compareAndSet(expected: Long, exchanged: Long): Boolean =
      if(underlying == expected)
        underlying = exchanged
        true
      else false
    override def compareAndExchangeAcquire(expected: Long, exchanged: Long): Long = compareAndExchange(expected, exchanged)
    override def compareAndExchangeRelease(expected: Long, exchanged: Long): Long = compareAndExchange(expected, exchanged)
    inline override def compareAndExchange(expected: Long, exchanged: Long): Long =
      if(underlying == expected) {
        underlying = exchanged
        expected
      } else underlying
    override def weakCompareAndSetPlain(expected: Long, exchanged: Long): Boolean = compareAndSet(expected, exchanged)
    override def weakCompareAndSetAcquire(expected: Long, exchanged: Long): Boolean = compareAndSet(expected, exchanged)
    override def weakCompareAndSetRelease(expected: Long, exchanged: Long): Boolean = compareAndSet(expected, exchanged)
    override def weakCompareAndSet(expected: Long, exchanged: Long): Boolean = compareAndSet(expected, exchanged)
    override def setPlain(value: Long): Unit = set(value)
    override def setOpaque(value: Long): Unit = set(value)
    override def setRelease(value: Long): Unit = set(value)
    inline override def set(value: Long): Unit = underlying = value
    override def getPlain: Long = get
    override def getOpaque: Long = get
    override def getAcquire: Long = get
    inline override def get: Long = underlying
    inline override def getAndBitwiseOr(value: Long): Long = {
      val prev = underlying
      underlying = underlying | value
      prev
    }
    override def getAndBitwiseOrAcquire(value: Long): Long = getAndBitwiseOr(value)
    override def getAndBitwiseOrRelease(value: Long): Long = getAndBitwiseOr(value)
    inline override def getAndBitwiseAnd(value: Long): Long = {
      val prev = underlying
      underlying = underlying & value
      prev
    }
    override def getAndBitwiseAndAcquire(value: Long): Long = getAndBitwiseAnd(value)
    override def getAndBitwiseAndRelease(value: Long): Long = getAndBitwiseAnd(value)
    inline override def getAndBitwiseXor(value: Long): Long = {
      val prev = underlying
      underlying = underlying & value
      prev
    }
    override def getAndBitwiseXorAcquire(value: Long): Long = getAndBitwiseXor(value)
    override def getAndBitwiseXorRelease(value: Long): Long = getAndBitwiseXor(value)
    inline override def getAndAdd(value: Long): Long = {
      val prev = underlying
      underlying = underlying + value
      prev
    }
    override def getAndAddAcquire(value: Long): Long = getAndAdd(value)
    override def getAndAddRelease(value: Long): Long = getAndAdd(value)
  end long

  override def float(init: Float): AtomicFloat = new:
    private var underlying: Float = init
    override def getAndSetAcquire(value: Float): Float = getAndSet(value)
    override def getAndSetRelease(value: Float): Float = getAndSet(value)
    inline def getAndSet(value: Float): Float = {
      val prev = underlying
      underlying = value
      prev
    }
    override def compareAndSet(expected: Float, exchanged: Float): Boolean =
      if(underlying == expected)
        underlying = exchanged
        true
      else false
    override def compareAndExchangeAcquire(expected: Float, exchanged: Float): Float = compareAndExchange(expected, exchanged)
    override def compareAndExchangeRelease(expected: Float, exchanged: Float): Float = compareAndExchange(expected, exchanged)
    inline override def compareAndExchange(expected: Float, exchanged: Float): Float =
      if(underlying == expected) {
        underlying = exchanged
        expected
      } else underlying
    override def weakCompareAndSetPlain(expected: Float, exchanged: Float): Boolean = compareAndSet(expected, exchanged)
    override def weakCompareAndSetAcquire(expected: Float, exchanged: Float): Boolean = compareAndSet(expected, exchanged)
    override def weakCompareAndSetRelease(expected: Float, exchanged: Float): Boolean = compareAndSet(expected, exchanged)
    override def weakCompareAndSet(expected: Float, exchanged: Float): Boolean = compareAndSet(expected, exchanged)
    override def setPlain(value: Float): Unit = set(value)
    override def setOpaque(value: Float): Unit = set(value)
    override def setRelease(value: Float): Unit = set(value)
    inline override def set(value: Float): Unit = underlying = value
    override def getPlain: Float = get
    override def getOpaque: Float = get
    override def getAcquire: Float = get
    inline override def get: Float = underlying
    inline override def getAndAdd(value: Float): Float = {
      val prev = underlying
      underlying = underlying + value
      prev
    }
    override def getAndAddAcquire(value: Float): Float = getAndAdd(value)
    override def getAndAddRelease(value: Float): Float = getAndAdd(value)
  end float

  override def double(init: Double): AtomicDouble = new:
    private var underlying: Double = init
    override def getAndSetAcquire(value: Double): Double = getAndSet(value)
    override def getAndSetRelease(value: Double): Double = getAndSet(value)
    inline def getAndSet(value: Double): Double = {
      val prev = underlying
      underlying = value
      prev
    }
    override def compareAndSet(expected: Double, exchanged: Double): Boolean =
      if(underlying == expected)
        underlying = exchanged
        true
      else false
    override def compareAndExchangeAcquire(expected: Double, exchanged: Double): Double = compareAndExchange(expected, exchanged)
    override def compareAndExchangeRelease(expected: Double, exchanged: Double): Double = compareAndExchange(expected, exchanged)
    inline override def compareAndExchange(expected: Double, exchanged: Double): Double =
      if(underlying == expected) {
        underlying = exchanged
        expected
      } else underlying
    override def weakCompareAndSetPlain(expected: Double, exchanged: Double): Boolean = compareAndSet(expected, exchanged)
    override def weakCompareAndSetAcquire(expected: Double, exchanged: Double): Boolean = compareAndSet(expected, exchanged)
    override def weakCompareAndSetRelease(expected: Double, exchanged: Double): Boolean = compareAndSet(expected, exchanged)
    override def weakCompareAndSet(expected: Double, exchanged: Double): Boolean = compareAndSet(expected, exchanged)
    override def setPlain(value: Double): Unit = set(value)
    override def setOpaque(value: Double): Unit = set(value)
    override def setRelease(value: Double): Unit = set(value)
    inline override def set(value: Double): Unit = underlying = value
    override def getPlain: Double = get
    override def getOpaque: Double = get
    override def getAcquire: Double = get
    inline override def get: Double = underlying
    inline override def getAndAdd(value: Double): Double = {
      val prev = underlying
      underlying = underlying + value
      prev
    }
    override def getAndAddAcquire(value: Double): Double = getAndAdd(value)
    override def getAndAddRelease(value: Double): Double = getAndAdd(value)
  end double


}

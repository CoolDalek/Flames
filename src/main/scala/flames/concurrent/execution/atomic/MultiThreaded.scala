package flames.concurrent.execution.atomic

import flames.concurrent.execution.handles.VarHandle
import flames.concurrent.execution.handles.VarHandle.Magnet

import scala.annotation.static

object MultiThreaded extends AtomicFactory:

  override def ref[T <: AnyRef](init: T): AtomicRef[T] = RefImpl[T](init)
  private object RefImpl:
    @static private final val refVh: VarHandle[RefImpl[AnyRef], AnyRef, "underlying"] =
      VarHandle[RefImpl[AnyRef]](_.underlying)
  private class RefImpl[T <: AnyRef](private var underlying: T) extends AtomicRef[T]:
    private inline def vh = RefImpl.refVh.asInstanceOf[VarHandle[RefImpl[T], T, "underlying"]]
    private inline given Magnet[RefImpl[T], T, "underlying"] = vh.magnetize(this)
    override def getAndSetAcquire(value: T): T = vh.getAndSetAcquire(value)
    override def getAndSetRelease(value: T): T = vh.getAndSetRelease(value)
    override def getAndSet(value: T): T = vh.getAndSetVolatile(value)
    override def compareAndSet(expected: T, exchanged: T): Boolean =
      vh.compareAndSetVolatile(expected, exchanged)
    override def compareAndExchangeAcquire(expected: T, exchanged: T): T =
      vh.compareAndExchangeAcquire(expected, exchanged)
    override def compareAndExchangeRelease(expected: T, exchanged: T): T =
      vh.compareAndExchangeRelease(expected, exchanged)
    override def compareAndExchange(expected: T, exchanged: T): T =
      vh.compareAndExchangeVolatile(expected, exchanged)
    override def weakCompareAndSetPlain(expected: T, exchanged: T): Boolean =
      vh.weakCompareAndSetPlain(expected, exchanged)
    override def weakCompareAndSetAcquire(expected: T, exchanged: T): Boolean =
      vh.weakCompareAndSetAcquire(expected, exchanged)
    override def weakCompareAndSetRelease(expected: T, exchanged: T): Boolean =
      vh.weakCompareAndSetRelease(expected, exchanged)
    override def weakCompareAndSet(expected: T, exchanged: T): Boolean =
      vh.weakCompareAndSetVolatile(expected, exchanged)
    override def setPlain(value: T): Unit = vh.setPlain(value)
    override def setOpaque(value: T): Unit = vh.setOpaque(value)
    override def setRelease(value: T): Unit = vh.setRelease(value)
    override def set(value: T): Unit = vh.setVolatile(value)
    override def getPlain: T = vh.getPlain
    override def getOpaque: T = vh.getOpaque
    override def getAcquire: T = vh.getAcquire
    override def get: T = vh.getVolatile
  end RefImpl

  override def boolean(init: Boolean): AtomicBool = BoolImpl(init)
  private object BoolImpl:
    @static private final val boolVh: VarHandle[BoolImpl, Boolean, "underlying"] =
      VarHandle[BoolImpl](_.underlying)
  private class BoolImpl(private var underlying: Boolean) extends AtomicBool:
    import BoolImpl.*
    private inline given Magnet[BoolImpl, Boolean, "underlying"] = boolVh.magnetize(this)
    override def getAndSetAcquire(value: Boolean): Boolean = boolVh.getAndSetAcquire(value)
    override def getAndSetRelease(value: Boolean): Boolean = boolVh.getAndSetRelease(value)
    override def getAndSet(value: Boolean): Boolean = boolVh.getAndSetVolatile(value)
    override def compareAndSet(expected: Boolean, exchanged: Boolean): Boolean =
      boolVh.compareAndSetVolatile(expected, exchanged)
    override def compareAndExchangeAcquire(expected: Boolean, exchanged: Boolean): Boolean =
      boolVh.compareAndExchangeAcquire(expected, exchanged)
    override def compareAndExchangeRelease(expected: Boolean, exchanged: Boolean): Boolean =
      boolVh.compareAndExchangeRelease(expected, exchanged)
    override def compareAndExchange(expected: Boolean, exchanged: Boolean): Boolean =
      boolVh.compareAndExchangeVolatile(expected, exchanged)
    override def weakCompareAndSetPlain(expected: Boolean, exchanged: Boolean): Boolean =
      boolVh.weakCompareAndSetPlain(expected, exchanged)
    override def weakCompareAndSetAcquire(expected: Boolean, exchanged: Boolean): Boolean =
      boolVh.weakCompareAndSetAcquire(expected, exchanged)
    override def weakCompareAndSetRelease(expected: Boolean, exchanged: Boolean): Boolean =
      boolVh.weakCompareAndSetRelease(expected, exchanged)
    override def weakCompareAndSet(expected: Boolean, exchanged: Boolean): Boolean =
      boolVh.weakCompareAndSetVolatile(expected, exchanged)
    override def setPlain(value: Boolean): Unit = boolVh.setPlain(value)
    override def setOpaque(value: Boolean): Unit = boolVh.setOpaque(value)
    override def setRelease(value: Boolean): Unit = boolVh.setRelease(value)
    override def set(value: Boolean): Unit = boolVh.setVolatile(value)
    override def getPlain: Boolean = boolVh.getPlain
    override def getOpaque: Boolean = boolVh.getOpaque
    override def getAcquire: Boolean = boolVh.getAcquire
    override def get: Boolean = boolVh.getVolatile
  end BoolImpl

  override def byte(init: Byte): AtomicByte = ByteImpl(init)
  private object ByteImpl:
    @static private final val byteVh: VarHandle[ByteImpl, Byte, "underlying"] =
      VarHandle[ByteImpl](_.underlying)
  private class ByteImpl(private var underlying: Byte) extends AtomicByte:
    import ByteImpl.*
    private inline given Magnet[ByteImpl, Byte, "underlying"] = byteVh.magnetize(this)
    override def getAndSetAcquire(value: Byte): Byte = byteVh.getAndSetAcquire(value)
    override def getAndSetRelease(value: Byte): Byte = byteVh.getAndSetRelease(value)
    override def getAndSet(value: Byte): Byte = byteVh.getAndSetVolatile(value)
    override def compareAndSet(expected: Byte, exchanged: Byte): Boolean =
      byteVh.compareAndSetVolatile(expected, exchanged)
    override def compareAndExchangeAcquire(expected: Byte, exchanged: Byte): Byte =
      byteVh.compareAndExchangeAcquire(expected, exchanged)
    override def compareAndExchangeRelease(expected: Byte, exchanged: Byte): Byte =
      byteVh.compareAndExchangeRelease(expected, exchanged)
    override def compareAndExchange(expected: Byte, exchanged: Byte): Byte =
      byteVh.compareAndExchangeVolatile(expected, exchanged)
    override def weakCompareAndSetPlain(expected: Byte, exchanged: Byte): Boolean =
      byteVh.weakCompareAndSetPlain(expected, exchanged)
    override def weakCompareAndSetAcquire(expected: Byte, exchanged: Byte): Boolean =
      byteVh.weakCompareAndSetAcquire(expected, exchanged)
    override def weakCompareAndSetRelease(expected: Byte, exchanged: Byte): Boolean =
      byteVh.weakCompareAndSetRelease(expected, exchanged)
    override def weakCompareAndSet(expected: Byte, exchanged: Byte): Boolean =
      byteVh.weakCompareAndSetVolatile(expected, exchanged)
    override def setPlain(value: Byte): Unit = byteVh.setPlain(value)
    override def setOpaque(value: Byte): Unit = byteVh.setOpaque(value)
    override def setRelease(value: Byte): Unit = byteVh.setRelease(value)
    override def set(value: Byte): Unit = byteVh.setVolatile(value)
    override def getPlain: Byte = byteVh.getPlain
    override def getOpaque: Byte = byteVh.getOpaque
    override def getAcquire: Byte = byteVh.getAcquire
    override def get: Byte = byteVh.getVolatile
    override def getAndBitwiseOr(value: Byte): Byte = byteVh.getAndBitwiseOrVolatile(value)
    override def getAndBitwiseOrAcquire(value: Byte): Byte = byteVh.getAndBitwiseOrAcquire(value)
    override def getAndBitwiseOrRelease(value: Byte): Byte = byteVh.getAndBitwiseOrRelease(value)
    override def getAndBitwiseAnd(value: Byte): Byte = byteVh.getAndBitwiseAndVolatile(value)
    override def getAndBitwiseAndAcquire(value: Byte): Byte = byteVh.getAndBitwiseAndAcquire(value)
    override def getAndBitwiseAndRelease(value: Byte): Byte = byteVh.getAndBitwiseAndRelease(value)
    override def getAndBitwiseXor(value: Byte): Byte = byteVh.getAndBitwiseXorVolatile(value)
    override def getAndBitwiseXorAcquire(value: Byte): Byte = byteVh.getAndBitwiseXorAcquire(value)
    override def getAndBitwiseXorRelease(value: Byte): Byte = byteVh.getAndBitwiseXorRelease(value)
    override def getAndAdd(value: Byte): Byte = byteVh.getAndAddVolatile(value)
    override def getAndAddAcquire(value: Byte): Byte = byteVh.getAndAddAcquire(value)
    override def getAndAddRelease(value: Byte): Byte = byteVh.getAndAddRelease(value)
  end ByteImpl

  override def char(init: Char): AtomicChar = CharImpl(init)
  private object CharImpl:
    @static private final val charVh: VarHandle[CharImpl, Char, "underlying"] =
      VarHandle[CharImpl](_.underlying)
  private class CharImpl(private var underlying: Char) extends AtomicChar:
    import CharImpl.*
    private inline given Magnet[CharImpl, Char, "underlying"] = charVh.magnetize(this)
    override def getAndSetAcquire(value: Char): Char = charVh.getAndSetAcquire(value)
    override def getAndSetRelease(value: Char): Char = charVh.getAndSetRelease(value)
    override def getAndSet(value: Char): Char = charVh.getAndSetVolatile(value)
    override def compareAndSet(expected: Char, exchanged: Char): Boolean =
      charVh.compareAndSetVolatile(expected, exchanged)
    override def compareAndExchangeAcquire(expected: Char, exchanged: Char): Char =
      charVh.compareAndExchangeAcquire(expected, exchanged)
    override def compareAndExchangeRelease(expected: Char, exchanged: Char): Char =
      charVh.compareAndExchangeRelease(expected, exchanged)
    override def compareAndExchange(expected: Char, exchanged: Char): Char =
      charVh.compareAndExchangeVolatile(expected, exchanged)
    override def weakCompareAndSetPlain(expected: Char, exchanged: Char): Boolean =
      charVh.weakCompareAndSetPlain(expected, exchanged)
    override def weakCompareAndSetAcquire(expected: Char, exchanged: Char): Boolean =
      charVh.weakCompareAndSetAcquire(expected, exchanged)
    override def weakCompareAndSetRelease(expected: Char, exchanged: Char): Boolean =
      charVh.weakCompareAndSetRelease(expected, exchanged)
    override def weakCompareAndSet(expected: Char, exchanged: Char): Boolean =
      charVh.weakCompareAndSetVolatile(expected, exchanged)
    override def setPlain(value: Char): Unit = charVh.setPlain(value)
    override def setOpaque(value: Char): Unit = charVh.setOpaque(value)
    override def setRelease(value: Char): Unit = charVh.setRelease(value)
    override def set(value: Char): Unit = charVh.setVolatile(value)
    override def getPlain: Char = charVh.getPlain
    override def getOpaque: Char = charVh.getOpaque
    override def getAcquire: Char = charVh.getAcquire
    override def get: Char = charVh.getVolatile
    override def getAndBitwiseOr(value: Char): Char = charVh.getAndBitwiseOrVolatile(value)
    override def getAndBitwiseOrAcquire(value: Char): Char = charVh.getAndBitwiseOrAcquire(value)
    override def getAndBitwiseOrRelease(value: Char): Char = charVh.getAndBitwiseOrRelease(value)
    override def getAndBitwiseAnd(value: Char): Char = charVh.getAndBitwiseAndVolatile(value)
    override def getAndBitwiseAndAcquire(value: Char): Char = charVh.getAndBitwiseAndAcquire(value)
    override def getAndBitwiseAndRelease(value: Char): Char = charVh.getAndBitwiseAndRelease(value)
    override def getAndBitwiseXor(value: Char): Char = charVh.getAndBitwiseXorVolatile(value)
    override def getAndBitwiseXorAcquire(value: Char): Char = charVh.getAndBitwiseXorAcquire(value)
    override def getAndBitwiseXorRelease(value: Char): Char = charVh.getAndBitwiseXorRelease(value)
    override def getAndAdd(value: Char): Char = charVh.getAndAddVolatile(value)
    override def getAndAddAcquire(value: Char): Char = charVh.getAndAddAcquire(value)
    override def getAndAddRelease(value: Char): Char = charVh.getAndAddRelease(value)
  end CharImpl

  override def short(init: Short): AtomicShort = ShortImpl(init)
  private object ShortImpl:
    @static private final val shortVh: VarHandle[ShortImpl, Short, "underlying"] =
      VarHandle[ShortImpl](_.underlying)
  private class ShortImpl(private var underlying: Short) extends AtomicShort:
    import ShortImpl.*
    private inline given Magnet[ShortImpl, Short, "underlying"] = shortVh.magnetize(this)
    override def getAndSetAcquire(value: Short): Short = shortVh.getAndSetAcquire(value)
    override def getAndSetRelease(value: Short): Short = shortVh.getAndSetRelease(value)
    override def getAndSet(value: Short): Short = shortVh.getAndSetVolatile(value)
    override def compareAndSet(expected: Short, exchanged: Short): Boolean =
      shortVh.compareAndSetVolatile(expected, exchanged)
    override def compareAndExchangeAcquire(expected: Short, exchanged: Short): Short =
      shortVh.compareAndExchangeAcquire(expected, exchanged)
    override def compareAndExchangeRelease(expected: Short, exchanged: Short): Short =
      shortVh.compareAndExchangeRelease(expected, exchanged)
    override def compareAndExchange(expected: Short, exchanged: Short): Short =
      shortVh.compareAndExchangeVolatile(expected, exchanged)
    override def weakCompareAndSetPlain(expected: Short, exchanged: Short): Boolean =
      shortVh.weakCompareAndSetPlain(expected, exchanged)
    override def weakCompareAndSetAcquire(expected: Short, exchanged: Short): Boolean =
      shortVh.weakCompareAndSetAcquire(expected, exchanged)
    override def weakCompareAndSetRelease(expected: Short, exchanged: Short): Boolean =
      shortVh.weakCompareAndSetRelease(expected, exchanged)
    override def weakCompareAndSet(expected: Short, exchanged: Short): Boolean =
      shortVh.weakCompareAndSetVolatile(expected, exchanged)
    override def setPlain(value: Short): Unit = shortVh.setPlain(value)
    override def setOpaque(value: Short): Unit = shortVh.setOpaque(value)
    override def setRelease(value: Short): Unit = shortVh.setRelease(value)
    override def set(value: Short): Unit = shortVh.setVolatile(value)
    override def getPlain: Short = shortVh.getPlain
    override def getOpaque: Short = shortVh.getOpaque
    override def getAcquire: Short = shortVh.getAcquire
    override def get: Short = shortVh.getVolatile
    override def getAndBitwiseOr(value: Short): Short = shortVh.getAndBitwiseOrVolatile(value)
    override def getAndBitwiseOrAcquire(value: Short): Short = shortVh.getAndBitwiseOrAcquire(value)
    override def getAndBitwiseOrRelease(value: Short): Short = shortVh.getAndBitwiseOrRelease(value)
    override def getAndBitwiseAnd(value: Short): Short = shortVh.getAndBitwiseAndVolatile(value)
    override def getAndBitwiseAndAcquire(value: Short): Short = shortVh.getAndBitwiseAndAcquire(value)
    override def getAndBitwiseAndRelease(value: Short): Short = shortVh.getAndBitwiseAndRelease(value)
    override def getAndBitwiseXor(value: Short): Short = shortVh.getAndBitwiseXorVolatile(value)
    override def getAndBitwiseXorAcquire(value: Short): Short = shortVh.getAndBitwiseXorAcquire(value)
    override def getAndBitwiseXorRelease(value: Short): Short = shortVh.getAndBitwiseXorRelease(value)
    override def getAndAdd(value: Short): Short = shortVh.getAndAddVolatile(value)
    override def getAndAddAcquire(value: Short): Short = shortVh.getAndAddAcquire(value)
    override def getAndAddRelease(value: Short): Short = shortVh.getAndAddRelease(value)
  end ShortImpl

  override def int(init: Int): AtomicInt = IntImpl(init)
  private object IntImpl:
    @static private final val intVh: VarHandle[IntImpl, Int, "underlying"] =
      VarHandle[IntImpl](_.underlying)
  private class IntImpl(private var underlying: Int) extends AtomicInt:
    import IntImpl.*
    private inline given Magnet[IntImpl, Int, "underlying"] = intVh.magnetize(this)
    override def getAndSetAcquire(value: Int): Int = intVh.getAndSetAcquire(value)
    override def getAndSetRelease(value: Int): Int = intVh.getAndSetRelease(value)
    override def getAndSet(value: Int): Int = intVh.getAndSetVolatile(value)
    override def compareAndSet(expected: Int, exchanged: Int): Boolean =
      intVh.compareAndSetVolatile(expected, exchanged)
    override def compareAndExchangeAcquire(expected: Int, exchanged: Int): Int =
      intVh.compareAndExchangeAcquire(expected, exchanged)
    override def compareAndExchangeRelease(expected: Int, exchanged: Int): Int =
      intVh.compareAndExchangeRelease(expected, exchanged)
    override def compareAndExchange(expected: Int, exchanged: Int): Int =
      intVh.compareAndExchangeVolatile(expected, exchanged)
    override def weakCompareAndSetPlain(expected: Int, exchanged: Int): Boolean =
      intVh.weakCompareAndSetPlain(expected, exchanged)
    override def weakCompareAndSetAcquire(expected: Int, exchanged: Int): Boolean =
      intVh.weakCompareAndSetAcquire(expected, exchanged)
    override def weakCompareAndSetRelease(expected: Int, exchanged: Int): Boolean =
      intVh.weakCompareAndSetRelease(expected, exchanged)
    override def weakCompareAndSet(expected: Int, exchanged: Int): Boolean =
      intVh.weakCompareAndSetVolatile(expected, exchanged)
    override def setPlain(value: Int): Unit = intVh.setPlain(value)
    override def setOpaque(value: Int): Unit = intVh.setOpaque(value)
    override def setRelease(value: Int): Unit = intVh.setRelease(value)
    override def set(value: Int): Unit = intVh.setVolatile(value)
    override def getPlain: Int = intVh.getPlain
    override def getOpaque: Int = intVh.getOpaque
    override def getAcquire: Int = intVh.getAcquire
    override def get: Int = intVh.getVolatile
    override def getAndBitwiseOr(value: Int): Int = intVh.getAndBitwiseOrVolatile(value)
    override def getAndBitwiseOrAcquire(value: Int): Int = intVh.getAndBitwiseOrAcquire(value)
    override def getAndBitwiseOrRelease(value: Int): Int = intVh.getAndBitwiseOrRelease(value)
    override def getAndBitwiseAnd(value: Int): Int = intVh.getAndBitwiseAndVolatile(value)
    override def getAndBitwiseAndAcquire(value: Int): Int = intVh.getAndBitwiseAndAcquire(value)
    override def getAndBitwiseAndRelease(value: Int): Int = intVh.getAndBitwiseAndRelease(value)
    override def getAndBitwiseXor(value: Int): Int = intVh.getAndBitwiseXorVolatile(value)
    override def getAndBitwiseXorAcquire(value: Int): Int = intVh.getAndBitwiseXorAcquire(value)
    override def getAndBitwiseXorRelease(value: Int): Int = intVh.getAndBitwiseXorRelease(value)
    override def getAndAdd(value: Int): Int = intVh.getAndAddVolatile(value)
    override def getAndAddAcquire(value: Int): Int = intVh.getAndAddAcquire(value)
    override def getAndAddRelease(value: Int): Int = intVh.getAndAddRelease(value)
  end IntImpl

  override def long(init: Long): AtomicLong = LongImpl(init)
  private object LongImpl:
    @static private final val longVh: VarHandle[LongImpl, Long, "underlying"] =
      VarHandle[LongImpl](_.underlying)
  private class LongImpl(private var underlying: Long) extends AtomicLong:
    import LongImpl.*
    private inline given Magnet[LongImpl, Long, "underlying"] = longVh.magnetize(this)
    override def getAndSetAcquire(value: Long): Long = longVh.getAndSetAcquire(value)
    override def getAndSetRelease(value: Long): Long = longVh.getAndSetRelease(value)
    override def getAndSet(value: Long): Long = longVh.getAndSetVolatile(value)
    override def compareAndSet(expected: Long, exchanged: Long): Boolean =
      longVh.compareAndSetVolatile(expected, exchanged)
    override def compareAndExchangeAcquire(expected: Long, exchanged: Long): Long =
      longVh.compareAndExchangeAcquire(expected, exchanged)
    override def compareAndExchangeRelease(expected: Long, exchanged: Long): Long =
      longVh.compareAndExchangeRelease(expected, exchanged)
    override def compareAndExchange(expected: Long, exchanged: Long): Long =
      longVh.compareAndExchangeVolatile(expected, exchanged)
    override def weakCompareAndSetPlain(expected: Long, exchanged: Long): Boolean =
      longVh.weakCompareAndSetPlain(expected, exchanged)
    override def weakCompareAndSetAcquire(expected: Long, exchanged: Long): Boolean =
      longVh.weakCompareAndSetAcquire(expected, exchanged)
    override def weakCompareAndSetRelease(expected: Long, exchanged: Long): Boolean =
      longVh.weakCompareAndSetRelease(expected, exchanged)
    override def weakCompareAndSet(expected: Long, exchanged: Long): Boolean =
      longVh.weakCompareAndSetVolatile(expected, exchanged)
    override def setPlain(value: Long): Unit = longVh.setPlain(value)
    override def setOpaque(value: Long): Unit = longVh.setOpaque(value)
    override def setRelease(value: Long): Unit = longVh.setRelease(value)
    override def set(value: Long): Unit = longVh.setVolatile(value)
    override def getPlain: Long = longVh.getPlain
    override def getOpaque: Long = longVh.getOpaque
    override def getAcquire: Long = longVh.getAcquire
    override def get: Long = longVh.getVolatile
    override def getAndBitwiseOr(value: Long): Long = longVh.getAndBitwiseOrVolatile(value)
    override def getAndBitwiseOrAcquire(value: Long): Long = longVh.getAndBitwiseOrAcquire(value)
    override def getAndBitwiseOrRelease(value: Long): Long = longVh.getAndBitwiseOrRelease(value)
    override def getAndBitwiseAnd(value: Long): Long = longVh.getAndBitwiseAndVolatile(value)
    override def getAndBitwiseAndAcquire(value: Long): Long = longVh.getAndBitwiseAndAcquire(value)
    override def getAndBitwiseAndRelease(value: Long): Long = longVh.getAndBitwiseAndRelease(value)
    override def getAndBitwiseXor(value: Long): Long = longVh.getAndBitwiseXorVolatile(value)
    override def getAndBitwiseXorAcquire(value: Long): Long = longVh.getAndBitwiseXorAcquire(value)
    override def getAndBitwiseXorRelease(value: Long): Long = longVh.getAndBitwiseXorRelease(value)
    override def getAndAdd(value: Long): Long = longVh.getAndAddVolatile(value)
    override def getAndAddAcquire(value: Long): Long = longVh.getAndAddAcquire(value)
    override def getAndAddRelease(value: Long): Long = longVh.getAndAddRelease(value)
  end LongImpl

  override def float(init: Float): AtomicFloat = FloatImpl(init)
  private object FloatImpl:
    @static private final val floatVh: VarHandle[FloatImpl, Float, "underlying"] =
      VarHandle[FloatImpl](_.underlying)
  private class FloatImpl(private var underlying: Float) extends AtomicFloat:
    import FloatImpl.*
    private inline given Magnet[FloatImpl, Float, "underlying"] = floatVh.magnetize(this)
    override def getAndSetAcquire(value: Float): Float = floatVh.getAndSetAcquire(value)
    override def getAndSetRelease(value: Float): Float = floatVh.getAndSetRelease(value)
    override def getAndSet(value: Float): Float = floatVh.getAndSetVolatile(value)
    override def compareAndSet(expected: Float, exchanged: Float): Boolean =
      floatVh.compareAndSetVolatile(expected, exchanged)
    override def compareAndExchangeAcquire(expected: Float, exchanged: Float): Float =
      floatVh.compareAndExchangeAcquire(expected, exchanged)
    override def compareAndExchangeRelease(expected: Float, exchanged: Float): Float =
      floatVh.compareAndExchangeRelease(expected, exchanged)
    override def compareAndExchange(expected: Float, exchanged: Float): Float =
      floatVh.compareAndExchangeVolatile(expected, exchanged)
    override def weakCompareAndSetPlain(expected: Float, exchanged: Float): Boolean =
      floatVh.weakCompareAndSetPlain(expected, exchanged)
    override def weakCompareAndSetAcquire(expected: Float, exchanged: Float): Boolean =
      floatVh.weakCompareAndSetAcquire(expected, exchanged)
    override def weakCompareAndSetRelease(expected: Float, exchanged: Float): Boolean =
      floatVh.weakCompareAndSetRelease(expected, exchanged)
    override def weakCompareAndSet(expected: Float, exchanged: Float): Boolean =
      floatVh.weakCompareAndSetVolatile(expected, exchanged)
    override def setPlain(value: Float): Unit = floatVh.setPlain(value)
    override def setOpaque(value: Float): Unit = floatVh.setOpaque(value)
    override def setRelease(value: Float): Unit = floatVh.setRelease(value)
    override def set(value: Float): Unit = floatVh.setVolatile(value)
    override def getPlain: Float = floatVh.getPlain
    override def getOpaque: Float = floatVh.getOpaque
    override def getAcquire: Float = floatVh.getAcquire
    override def get: Float = floatVh.getVolatile
    override def getAndAdd(value: Float): Float = floatVh.getAndAddVolatile(value)
    override def getAndAddAcquire(value: Float): Float = floatVh.getAndAddAcquire(value)
    override def getAndAddRelease(value: Float): Float = floatVh.getAndAddRelease(value)
  end FloatImpl

  override def double(init: Double): AtomicDouble = DoubleImpl(init)
  private object DoubleImpl:
    @static private final val doubleVh: VarHandle[DoubleImpl, Double, "underlying"] =
      VarHandle[DoubleImpl](_.underlying)
  private class DoubleImpl(private var underlying: Double) extends AtomicDouble:
    import DoubleImpl.*
    private inline given Magnet[DoubleImpl, Double, "underlying"] = doubleVh.magnetize(this)
    override def getAndSetAcquire(value: Double): Double = doubleVh.getAndSetAcquire(value)
    override def getAndSetRelease(value: Double): Double = doubleVh.getAndSetRelease(value)
    override def getAndSet(value: Double): Double = doubleVh.getAndSetVolatile(value)
    override def compareAndSet(expected: Double, exchanged: Double): Boolean =
      doubleVh.compareAndSetVolatile(expected, exchanged)
    override def compareAndExchangeAcquire(expected: Double, exchanged: Double): Double =
      doubleVh.compareAndExchangeAcquire(expected, exchanged)
    override def compareAndExchangeRelease(expected: Double, exchanged: Double): Double =
      doubleVh.compareAndExchangeRelease(expected, exchanged)
    override def compareAndExchange(expected: Double, exchanged: Double): Double =
      doubleVh.compareAndExchangeVolatile(expected, exchanged)
    override def weakCompareAndSetPlain(expected: Double, exchanged: Double): Boolean =
      doubleVh.weakCompareAndSetPlain(expected, exchanged)
    override def weakCompareAndSetAcquire(expected: Double, exchanged: Double): Boolean =
      doubleVh.weakCompareAndSetAcquire(expected, exchanged)
    override def weakCompareAndSetRelease(expected: Double, exchanged: Double): Boolean =
      doubleVh.weakCompareAndSetRelease(expected, exchanged)
    override def weakCompareAndSet(expected: Double, exchanged: Double): Boolean =
      doubleVh.weakCompareAndSetVolatile(expected, exchanged)
    override def setPlain(value: Double): Unit = doubleVh.setPlain(value)
    override def setOpaque(value: Double): Unit = doubleVh.setOpaque(value)
    override def setRelease(value: Double): Unit = doubleVh.setRelease(value)
    override def set(value: Double): Unit = doubleVh.setVolatile(value)
    override def getPlain: Double = doubleVh.getPlain
    override def getOpaque: Double = doubleVh.getOpaque
    override def getAcquire: Double = doubleVh.getAcquire
    override def get: Double = doubleVh.getVolatile
    override def getAndAdd(value: Double): Double = doubleVh.getAndAddVolatile(value)
    override def getAndAddAcquire(value: Double): Double = doubleVh.getAndAddAcquire(value)
    override def getAndAddRelease(value: Double): Double = doubleVh.getAndAddRelease(value)
  end DoubleImpl

end MultiThreaded

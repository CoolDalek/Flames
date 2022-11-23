package flames.concurrent.execution.atomic

import flames.concurrent.utils.Summoner

trait AtomicMake[T]:

  type Result <: Atomic[T]

  transparent inline def make(init: T, factory: AtomicFactory): Result

object AtomicMake extends Summoner[AtomicMake]:

  inline given [T <: AnyRef]: AtomicMake[T] with
    type Result = AtomicRef[T]
    transparent inline def make(init: T, factory: AtomicFactory): AtomicRef[T] = factory.ref[T](init)
  end given

  inline given AtomicMake[Boolean] with
    type Result = AtomicBool
    transparent inline def make(init: Boolean, factory: AtomicFactory): AtomicBool = factory.boolean(init)
  end given

  inline given AtomicMake[Byte] with
    type Result = AtomicByte
    transparent inline def make(init: Byte, factory: AtomicFactory): AtomicByte = factory.byte(init)
  end given

  inline given AtomicMake[Char] with
    type Result = AtomicChar
    transparent inline def make(init: Char, factory: AtomicFactory): AtomicChar = factory.char(init)
  end given

  inline given AtomicMake[Short] with
    type Result = AtomicShort
    transparent inline def make(init: Short, factory: AtomicFactory): AtomicShort = factory.short(init)
  end given

  inline given AtomicMake[Int] with
    type Result = AtomicInt
    transparent inline def make(init: Int, factory: AtomicFactory): AtomicInt = factory.int(init)
  end given

  inline given AtomicMake[Long] with
    type Result = AtomicLong
    transparent inline def make(init: Long, factory: AtomicFactory): AtomicLong = factory.long(init)
  end given

  inline given AtomicMake[Float] with
    type Result = AtomicFloat
    transparent inline def make(init: Float, factory: AtomicFactory): AtomicFloat = factory.float(init)
  end given

  inline given AtomicMake[Double] with
    type Result = AtomicDouble
    transparent inline def make(init: Double, factory: AtomicFactory): AtomicDouble = factory.double(init)
  end given

end AtomicMake
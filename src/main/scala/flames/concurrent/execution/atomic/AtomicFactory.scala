package flames.concurrent.execution.atomic

trait AtomicFactory {

  def ref[T <: AnyRef](init: T): AtomicRef[T]

  def boolean(init: Boolean): AtomicBool

  def byte(init: Byte): AtomicByte

  def char(init: Char): AtomicChar

  def short(init: Short): AtomicShort

  def int(init: Int): AtomicInt

  def long(init: Long): AtomicLong

  def float(init: Float): AtomicFloat

  def double(init: Double): AtomicDouble
  
  inline def make[T](init: T)(using ev: AtomicMake[T]): ev.Result = ev.make(init, this)

}

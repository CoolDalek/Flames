package flames.concurrent.execution.handles

sealed trait PrimitiveNumber[T <: AnyVal]
sealed trait IntegralPrimitive[T <: AnyVal] extends PrimitiveNumber[T]
sealed trait FractionalPrimitive[T <: AnyVal] extends PrimitiveNumber[T]
object PrimitiveNumber:

  private inline def integral[T <: AnyVal]: IntegralPrimitive[T] = null.asInstanceOf[IntegralPrimitive[T]]
  private inline def fractional[T <: AnyVal]: FractionalPrimitive[T] = null.asInstanceOf[FractionalPrimitive[T]]

  inline given IntegralPrimitive[Byte] = integral[Byte]
  inline given IntegralPrimitive[Char] = integral[Char]
  inline given IntegralPrimitive[Short] = integral[Short]
  inline given IntegralPrimitive[Int] = integral[Int]
  inline given IntegralPrimitive[Long] = integral[Long]
  inline given FractionalPrimitive[Float] = fractional[Float]
  inline given FractionalPrimitive[Double] = fractional[Double]

end PrimitiveNumber
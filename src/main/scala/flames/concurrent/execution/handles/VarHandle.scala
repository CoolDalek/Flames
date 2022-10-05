package flames.concurrent.execution.handles

import java.lang.invoke.{MethodHandles, VarHandle as JVarHandle}
import scala.language.postfixOps
import scala.reflect.{ClassTag, classTag}

object VarHandle:
  type Tag[T] = VarGet[T] | VarSet[T] | VarHandle[T]
  opaque type VarGet[+T] = JVarHandle
  opaque type VarSet[-T] = JVarHandle
  opaque type VarHandle[T] <: VarGet[T] & VarSet[T] = JVarHandle

  extension [T, Handle[x] <: Tag[x]](self: Handle[T]) {

    inline def hasInvokeExactBehavior: Boolean =
      self.hasInvokeExactBehavior()

    inline def withInvokeBehavior: Handle[T] =
      self.withInvokeBehavior().asInstanceOf[Handle[T]]

    inline def withInvokeExactBehavior: Handle[T] =
      self.withInvokeExactBehavior().asInstanceOf[Handle[T]]

  }

  extension [T](self: VarGet[T]) {

    inline def getPlain[R](from: R)(using Field[T, R]): T =
      self.get(from).asInstanceOf[T]

    inline def getOpaque[R](from: R)(using Field[T, R]): T =
      self.getOpaque(from).asInstanceOf[T]

    inline def getAcquire[R](from: R)(using Field[T, R]): T =
      self.getAcquire(from).asInstanceOf[T]

    inline def getVolatile[R](from: R)(using Field[T, R]): T =
      self.getVolatile(from).asInstanceOf[T]

  }

  extension [T](self: VarSet[T]) {

    inline def setPlain[R](on: R, value: T)(using Field[T, R]): Unit =
      self.set(on, value)

    inline def setOpaque[R](on: R, value: T)(using Field[T, R]): Unit =
      self.setOpaque(on, value)

    inline def setRelease[R](on: R, value: T)(using Field[T, R]): Unit =
      self.setRelease(on, value)

    inline def setVolatile[R](on: R, value: T)(using Field[T, R]): Unit =
      self.setVolatile(on, value)

  }

  extension [T](self: VarHandle[T]) {

    inline def weakCompareAndSetPlain[R](where: R, expected: T, exchanged: T)(using Field[T, R]): Boolean =
      self.weakCompareAndSetPlain(where, expected, exchanged)

    inline def getAndSetAcquire[R](where: T, value: T)(using Field[T, R]): T =
      self.getAndSetAcquire(where, value).asInstanceOf[T]

    inline def compareAndExchangeAcquire[R](where: R, expected: T, exchanged: T)(using Field[T, R]): T =
      self.compareAndExchangeAcquire(where, expected, exchanged).asInstanceOf[T]

    inline def weakCompareAndSetAcquire[R](where: R, expected: T, exchanged: T)(using Field[T, R]): Boolean =
      self.weakCompareAndSetAcquire(where, expected, exchanged)

    inline def getAndSetRelease[R](where: R, value: T)(using Field[T, R]): T =
      self.getAndSetRelease(where, value).asInstanceOf[T]

    inline def compareAndExchangeRelease[R](where: R, expected: T, exchanged: T)(using Field[T, R]): T =
      self.compareAndExchangeRelease(where, expected, exchanged).asInstanceOf[T]

    inline def weakCompareAndSetRelease[R](where: R, expected: T, exchanged: T)(using Field[T, R]): Boolean =
      self.weakCompareAndSetRelease(where, expected, exchanged)

    inline def getAndSetVolatile[R](where: R, value: T)(using Field[T, R]): T =
      self.getAndSet(where, value).asInstanceOf[T]

    inline def compareAndSetVolatile[R](where: R, expected: T, exchanged: T)(using Field[T, R]): Boolean =
      self.compareAndSet(where, expected, exchanged)

    inline def compareAndExchangeVolatile[R](where: R, expected: T, exchanged: T)(using Field[T, R]): T =
      self.compareAndExchange(where, expected, exchanged).asInstanceOf[T]

    inline def weakCompareAndSetVolatile[R](where: R, expected: T, exchanged: T)(using Field[T, R]): Boolean =
      self.weakCompareAndSet(where, expected, exchanged)

  }

  object Magnet:

    extension[T] (self: VarGet[T]) {

      inline def getPlain[R](using from: Field.Magnet[T, R]): T =
        self.get(from).asInstanceOf[T]

      inline def getOpaque[R](using from: Field.Magnet[T, R]): T =
        self.getOpaque(from).asInstanceOf[T]

      inline def getAcquire[R](using from: Field.Magnet[T, R]): T =
        self.getAcquire(from).asInstanceOf[T]

      inline def getVolatile[R](using from: Field.Magnet[T, R]): T =
        self.getVolatile(from).asInstanceOf[T]

    }

    extension[T] (self: VarSet[T]) {

      inline def setPlain[R](value: T)(using on: Field.Magnet[T, R]): Unit =
        self.set(on, value)

      inline def setOpaque[R](value: T)(using on: Field.Magnet[T, R]): Unit =
        self.setOpaque(on, value)

      inline def setRelease[R](value: T)(using on: Field.Magnet[T, R]): Unit =
        self.setRelease(on, value)

      inline def setVolatile[R](value: T)(using on: Field.Magnet[T, R]): Unit =
        self.setVolatile(on, value)

    }

    extension[T] (self: VarHandle[T]) {

      inline def weakCompareAndSetPlain[R](expected: T, exchanged: T)(using where: Field.Magnet[T, R]): Boolean =
        self.weakCompareAndSetPlain(where, expected, exchanged)

      inline def getAndSetAcquire[R](value: T)(using where: Field.Magnet[T, R]): T =
        self.getAndSetAcquire(where, value).asInstanceOf[T]

      inline def compareAndExchangeAcquire[R](expected: T, exchanged: T)(using where: Field.Magnet[T, R]): T =
        self.compareAndExchangeAcquire(where, expected, exchanged).asInstanceOf[T]

      inline def weakCompareAndSetAcquire[R](expected: T, exchanged: T)(using where: Field.Magnet[T, R]): Boolean =
        self.weakCompareAndSetAcquire(where, expected, exchanged)

      inline def getAndSetRelease[R](value: T)(using where: Field.Magnet[T, R]): T =
        self.getAndSetRelease(where, value).asInstanceOf[T]

      inline def compareAndExchangeRelease[R](expected: T, exchanged: T)(using where: Field.Magnet[T, R]): T =
        self.compareAndExchangeRelease(where, expected, exchanged).asInstanceOf[T]

      inline def weakCompareAndSetRelease[R](expected: T, exchanged: T)(using where: Field.Magnet[T, R]): Boolean =
        self.weakCompareAndSetRelease(where, expected, exchanged)

      inline def getAndSetVolatile[R](value: T)(using where: Field.Magnet[T, R]): T =
        self.getAndSet(where, value).asInstanceOf[T]

      inline def compareAndSetVolatile[R](expected: T, exchanged: T)(using where: Field.Magnet[T, R]): Boolean =
        self.compareAndSet(where, expected, exchanged)

      inline def compareAndExchangeVolatile[R](expected: T, exchanged: T)(using where: Field.Magnet[T, R]): T =
        self.compareAndExchange(where, expected, exchanged).asInstanceOf[T]

      inline def weakCompareAndSetVolatile[R](expected: T, exchanged: T)(using where: Field.Magnet[T, R]): Boolean =
        self.weakCompareAndSet(where, expected, exchanged)

    }

  end Magnet

  object Scoped:

    extension[T] (self: VarGet[T]) {

      inline def getPlain(using from: Field.Scoped[T]): T =
        self.get(from).asInstanceOf[T]

      inline def getOpaque(using from: Field.Scoped[T]): T =
        self.getOpaque(from).asInstanceOf[T]

      inline def getAcquire(using from: Field.Scoped[T]): T =
        self.getAcquire(from).asInstanceOf[T]

      inline def getVolatile(using from: Field.Scoped[T]): T =
        self.getVolatile(from).asInstanceOf[T]

    }

    extension[T] (self: VarSet[T]) {

      inline def setPlain(value: T)(using on: Field.Scoped[T]): Unit =
        self.set(on, value)

      inline def setOpaque(value: T)(using on: Field.Scoped[T]): Unit =
        self.setOpaque(on, value)

      inline def setRelease(value: T)(using on: Field.Scoped[T]): Unit =
        self.setRelease(on, value)

      inline def setVolatile(value: T)(using on: Field.Scoped[T]): Unit =
        self.setVolatile(on, value)

    }

    extension[T] (self: VarHandle[T]) {

      inline def weakCompareAndSetPlain(expected: T, exchanged: T)(using where: Field.Scoped[T]): Boolean =
        self.weakCompareAndSetPlain(where, expected, exchanged)

      inline def getAndSetAcquire(value: T)(using where: Field.Scoped[T]): T =
        self.getAndSetAcquire(where, value).asInstanceOf[T]

      inline def compareAndExchangeAcquire(expected: T, exchanged: T)(using where: Field.Scoped[T]): T =
        self.compareAndExchangeAcquire(where, expected, exchanged).asInstanceOf[T]

      inline def weakCompareAndSetAcquire(expected: T, exchanged: T)(using where: Field.Scoped[T]): Boolean =
        self.weakCompareAndSetAcquire(where, expected, exchanged)

      inline def getAndSetRelease(value: T)(using where: Field.Scoped[T]): T =
        self.getAndSetRelease(where, value).asInstanceOf[T]

      inline def compareAndExchangeRelease(expected: T, exchanged: T)(using where: Field.Scoped[T]): T =
        self.compareAndExchangeRelease(where, expected, exchanged).asInstanceOf[T]

      inline def weakCompareAndSetRelease(expected: T, exchanged: T)(using where: Field.Scoped[T]): Boolean =
        self.weakCompareAndSetRelease(where, expected, exchanged)

      inline def getAndSetVolatile(value: T)(using where: Field.Scoped[T]): T =
        self.getAndSet(where, value).asInstanceOf[T]

      inline def compareAndSetVolatile(expected: T, exchanged: T)(using where: Field.Scoped[T]): Boolean =
        self.compareAndSet(where, expected, exchanged)

      inline def compareAndExchangeVolatile(expected: T, exchanged: T)(using where: Field.Scoped[T]): T =
        self.compareAndExchange(where, expected, exchanged).asInstanceOf[T]

      inline def weakCompareAndSetVolatile(expected: T, exchanged: T)(using where: Field.Scoped[T]): Boolean =
        self.weakCompareAndSet(where, expected, exchanged)

    }

  end Scoped

  inline private[handles] def unsafeCoerce[T, Handle[x] <: Tag[x]](handle: JVarHandle): Handle[T] = handle.asInstanceOf[Handle[T]]

  import scala.reflect.*

  type Apply[T, R] = Field.Aux[T, R, VarHandle] | Field.Aux[T, R, VarGet]

  transparent inline def apply[R: ClassTag, T: ClassTag](inline getter: R => T): Apply[T, R] =
    Macro.varHandleMacro[R, T](
      getter,
      classTag[R].runtimeClass.asInstanceOf[Class[R]],
      classTag[T].runtimeClass.asInstanceOf[Class[T]],
    )

end VarHandle
export VarHandle.{VarGet, VarSet, VarHandle}
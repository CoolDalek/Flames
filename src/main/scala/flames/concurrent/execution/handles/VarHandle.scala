package flames.concurrent.execution.handles

import java.lang.invoke.{MethodHandles, VarHandle as JVarHandle}
import scala.annotation.targetName
import scala.reflect.{ClassTag, classTag}

object VarHandle:
  type VarName = String & Singleton
  type Tag[C, V, N <: VarName] = VarGet[C, V, N] | VarSet[C, V, N] | VarHandle[C, V, N]
  opaque type VarGet[-Class, +Var, Name <: VarName] = JVarHandle
  opaque type VarSet[Class, -Var, Name <: VarName] = JVarHandle
  opaque type VarHandle[Class, Var, Name <: VarName] <: VarGet[Class, Var, Name] & VarSet[Class, Var, Name] = JVarHandle

  opaque type Magnet[C, V, N <: VarName] = C

  extension [C, V, N <: VarName](self: Magnet[C, V, N]) {

    inline def underlying: C = self

  }

  extension [C, V, N <: VarName, Handle[x, y, z <: VarName] <: Tag[x, y, z]](self: Handle[C, V, N]) {

    inline def hasInvokeExactBehavior: Boolean =
      self.hasInvokeExactBehavior()

    inline def withInvokeBehavior: Handle[C, V, N] =
      self.withInvokeBehavior().asInstanceOf[Handle[C, V, N]]

    inline def withInvokeExactBehavior: Handle[C, V, N] =
      self.withInvokeExactBehavior().asInstanceOf[Handle[C, V, N]]

    inline def magnetize(instance: C): Magnet[C, V, N] = instance

  }

  extension [C, V, N <: VarName](self: VarGet[C, V, N]) {

    @targetName("getPE")
    inline def getPlain(from: C): V =
      self.get(from).asInstanceOf[V]

    @targetName("getOE")
    inline def getOpaque(from: C): V =
      self.getOpaque(from).asInstanceOf[V]

    @targetName("getAE")
    inline def getAcquire(from: C): V =
      self.getAcquire(from).asInstanceOf[V]

    @targetName("getVE")
    inline def getVolatile(from: C): V =
      self.getVolatile(from).asInstanceOf[V]

    @targetName("getPI")
    inline def getPlain(using from: Magnet[C, V, N]): V =
      self.get(from).asInstanceOf[V]

    @targetName("getOI")
    inline def getOpaque(using from: Magnet[C, V, N]): V =
      self.getOpaque(from).asInstanceOf[V]

    @targetName("getAI")
    inline def getAcquire(using from: Magnet[C, V, N]): V =
      self.getAcquire(from).asInstanceOf[V]

    @targetName("getVI")
    inline def getVolatile(using from: Magnet[C, V, N]): V =
      self.getVolatile(from).asInstanceOf[V]

  }

  extension[C, V, N <: VarName] (self: VarSet[C, V, N]) {

    @targetName("setPE")
    inline def setPlain(on: C, value: V): Unit =
      self.set(on, value)

    @targetName("setOE")
    inline def setOpaque(on: C, value: V): Unit =
      self.setOpaque(on, value)

    @targetName("setRE")
    inline def setRelease(on: C, value: V): Unit =
      self.setRelease(on, value)

    @targetName("setVE")
    inline def setVolatile(on: C, value: V): Unit =
      self.setVolatile(on, value)

    @targetName("setPI")
    inline def setPlain(value: V)(using on: Magnet[C, V, N]): Unit =
      self.set(on, value)

    @targetName("setOI")
    inline def setOpaque(value: V)(using on: Magnet[C, V, N]): Unit =
      self.setOpaque(on, value)

    @targetName("setRI")
    inline def setRelease(value: V)(using on: Magnet[C, V, N]): Unit =
      self.setRelease(on, value)

    @targetName("setVI")
    inline def setVolatile(value: V)(using on: Magnet[C, V, N]): Unit =
      self.setVolatile(on, value)

  }

  extension[C, V, N <: VarName] (self: VarHandle[C, V, N]) {

    @targetName("getSetAE")
    inline def getAndSetAcquire(where: V, value: V): V =
      self.getAndSetAcquire(where, value).asInstanceOf[V]

    @targetName("getSetRE")
    inline def getAndSetRelease(where: C, value: V): V =
      self.getAndSetRelease(where, value).asInstanceOf[V]

    @targetName("getSetVE")
    inline def getAndSetVolatile(where: C, value: V): V =
      self.getAndSet(where, value).asInstanceOf[V]

    @targetName("wCasPE")
    inline def weakCompareAndSetPlain(where: C, expected: V, exchanged: V): Boolean =
      self.weakCompareAndSetPlain(where, expected, exchanged)

    @targetName("wCasAE")
    inline def weakCompareAndSetAcquire(where: C, expected: V, exchanged: V): Boolean =
      self.weakCompareAndSetAcquire(where, expected, exchanged)

    @targetName("wCasRE")
    inline def weakCompareAndSetRelease(where: C, expected: V, exchanged: V): Boolean =
      self.weakCompareAndSetRelease(where, expected, exchanged)

    @targetName("wCasVE")
    inline def weakCompareAndSetVolatile(where: C, expected: V, exchanged: V): Boolean =
      self.weakCompareAndSet(where, expected, exchanged)

    @targetName("caeAE")
    inline def compareAndExchangeAcquire(where: C, expected: V, exchanged: V): V =
      self.compareAndExchangeAcquire(where, expected, exchanged).asInstanceOf[V]

    @targetName("caeRE")
    inline def compareAndExchangeRelease(where: C, expected: V, exchanged: V): V =
      self.compareAndExchangeRelease(where, expected, exchanged).asInstanceOf[V]

    @targetName("caeVE")
    inline def compareAndExchangeVolatile(where: C, expected: V, exchanged: V): V =
      self.compareAndExchange(where, expected, exchanged).asInstanceOf[V]

    @targetName("casVE")
    inline def compareAndSetVolatile(where: C, expected: V, exchanged: V): Boolean =
      self.compareAndSet(where, expected, exchanged)

    @targetName("getSetAI")
    inline def getAndSetAcquire(value: V)(using where: Magnet[C, V, N]): V =
      self.getAndSetAcquire(where, value).asInstanceOf[V]

    @targetName("getSetRI")
    inline def getAndSetRelease(value: V)(using where: Magnet[C, V, N]): V =
      self.getAndSetRelease(where, value).asInstanceOf[V]

    @targetName("getSetVI")
    inline def getAndSetVolatile(value: V)(using where: Magnet[C, V, N]): V =
      self.getAndSet(where, value).asInstanceOf[V]

    @targetName("wCasPI")
    inline def weakCompareAndSetPlain(expected: V, exchanged: V)(using where: Magnet[C, V, N]): Boolean =
      self.weakCompareAndSetPlain(where, expected, exchanged)

    @targetName("wCasAI")
    inline def weakCompareAndSetAcquire(expected: V, exchanged: V)(using where: Magnet[C, V, N]): Boolean =
      self.weakCompareAndSetAcquire(where, expected, exchanged)

    @targetName("wCasRI")
    inline def weakCompareAndSetRelease(expected: V, exchanged: V)(using where: Magnet[C, V, N]): Boolean =
      self.weakCompareAndSetRelease(where, expected, exchanged)

    @targetName("wCasVI")
    inline def weakCompareAndSetVolatile(expected: V, exchanged: V)(using where: Magnet[C, V, N]): Boolean =
      self.weakCompareAndSet(where, expected, exchanged)

    @targetName("caeAI")
    inline def compareAndExchangeAcquire(expected: V, exchanged: V)(using where: Magnet[C, V, N]): V =
      self.compareAndExchangeAcquire(where, expected, exchanged).asInstanceOf[V]

    @targetName("caeRI")
    inline def compareAndExchangeRelease(expected: V, exchanged: V)(using where: Magnet[C, V, N]): V =
      self.compareAndExchangeRelease(where, expected, exchanged).asInstanceOf[V]

    @targetName("caeVI")
    inline def compareAndExchangeVolatile(expected: V, exchanged: V)(using where: Magnet[C, V, N]): V =
      self.compareAndExchange(where, expected, exchanged).asInstanceOf[V]

    @targetName("casVI")
    inline def compareAndSetVolatile(expected: V, exchanged: V)(using where: Magnet[C, V, N]): Boolean =
      self.compareAndSet(where, expected, exchanged)

  }

  import scala.reflect.*

  transparent inline def apply[C: ClassTag, V: ClassTag](inline getter: C => V): Any =
    Macro.vhMacro[C, V](
      getter,
      classTag[C].runtimeClass.asInstanceOf[Class[C]],
      classTag[V].runtimeClass.asInstanceOf[Class[V]],
    )

end VarHandle
export VarHandle.{VarGet, VarSet, VarHandle, Magnet}
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

  extension [C, V, N <: VarName](self: Magnet[C, V, N])
    inline def underlying: C = self

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

  }

  extension [C, V, N <: VarName](self: VarGet[C, V, N])(using from: Magnet[C, V, N]) {

    @targetName("getPI")
    inline def getPlain: V =
      self.get(from).asInstanceOf[V]

    @targetName("getOI")
    inline def getOpaque: V =
      self.getOpaque(from).asInstanceOf[V]

    @targetName("getAI")
    inline def getAcquire: V =
      self.getAcquire(from).asInstanceOf[V]

    @targetName("getVI")
    inline def getVolatile: V =
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

  }

  extension[C, V, N <: VarName] (self: VarSet[C, V, N])(using on: Magnet[C, V, N]) {

    @targetName("setPI")
    inline def setPlain(value: V): Unit =
      self.set(on, value)

    @targetName("setOI")
    inline def setOpaque(value: V): Unit =
      self.setOpaque(on, value)

    @targetName("setRI")
    inline def setRelease(value: V): Unit =
      self.setRelease(on, value)

    @targetName("setVI")
    inline def setVolatile(value: V): Unit =
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

  }

  extension[C, V, N <: VarName] (self: VarHandle[C, V, N])(using where: Magnet[C, V, N]) {

    @targetName("getSetAI")
    inline def getAndSetAcquire(value: V): V =
      self.getAndSetAcquire(where, value).asInstanceOf[V]

    @targetName("getSetRI")
    inline def getAndSetRelease(value: V): V =
      self.getAndSetRelease(where, value).asInstanceOf[V]

    @targetName("getSetVI")
    inline def getAndSetVolatile(value: V): V =
      self.getAndSet(where, value).asInstanceOf[V]

    @targetName("wCasPI")
    inline def weakCompareAndSetPlain(expected: V, exchanged: V): Boolean =
      self.weakCompareAndSetPlain(where, expected, exchanged)

    @targetName("wCasAI")
    inline def weakCompareAndSetAcquire(expected: V, exchanged: V): Boolean =
      self.weakCompareAndSetAcquire(where, expected, exchanged)

    @targetName("wCasRI")
    inline def weakCompareAndSetRelease(expected: V, exchanged: V): Boolean =
      self.weakCompareAndSetRelease(where, expected, exchanged)

    @targetName("wCasVI")
    inline def weakCompareAndSetVolatile(expected: V, exchanged: V): Boolean =
      self.weakCompareAndSet(where, expected, exchanged)

    @targetName("caeAI")
    inline def compareAndExchangeAcquire(expected: V, exchanged: V): V =
      self.compareAndExchangeAcquire(where, expected, exchanged).asInstanceOf[V]

    @targetName("caeRI")
    inline def compareAndExchangeRelease(expected: V, exchanged: V): V =
      self.compareAndExchangeRelease(where, expected, exchanged).asInstanceOf[V]

    @targetName("caeVI")
    inline def compareAndExchangeVolatile(expected: V, exchanged: V): V =
      self.compareAndExchange(where, expected, exchanged).asInstanceOf[V]

    @targetName("casVI")
    inline def compareAndSetVolatile(expected: V, exchanged: V): Boolean =
      self.compareAndSet(where, expected, exchanged)

  }
  
  extension[C, V <: AnyVal, N <: VarName] (self: VarHandle[C, V, N])(using PrimitiveNumber[V]) {

    @targetName("getAddVE")
    inline def getAndAddVolatile(where: C, value: V): V =
      self.getAndAdd(where, value).asInstanceOf[V]

    @targetName("getAddAE")
    inline def getAndAddAcquire(where: C, value: V): V =
      self.getAndSetAcquire(where, value).asInstanceOf[V]

    @targetName("getAddRE")
    inline def getAndAddRelease(where: C, value: V): V =
      self.getAndSetRelease(where, value).asInstanceOf[V]
    
  }
  
  extension[C, V <: AnyVal, N <: VarName] (self: VarHandle[C, V, N])
                                          (using ev: PrimitiveNumber[V], where: Magnet[C, V, N]) {

    @targetName("getAddVI")
    inline def getAndAddVolatile(value: V): V =
      self.getAndAdd(where, value).asInstanceOf[V]

    @targetName("getAddAI")
    inline def getAndAddAcquire(value: V): V =
      self.getAndSetAcquire(where, value).asInstanceOf[V]

    @targetName("getAddRI")
    inline def getAndAddRelease(value: V): V =
      self.getAndSetRelease(where, value).asInstanceOf[V]
    
  }
  
  extension[C, V <: AnyVal, N <: VarName] (self: VarHandle[C, V, N])(using IntegralPrimitive[V]) {

    @targetName("getOrVE")
    inline def getAndBitwiseOrVolatile(where: C, value: V): V =
      self.getAndBitwiseOr(where, value).asInstanceOf[V]

    @targetName("getOrAE")
    inline def getAndBitwiseOrAcquire(where: C, value: V): V =
      self.getAndBitwiseOrAcquire(where, value).asInstanceOf[V]

    @targetName("getOrRE")
    inline def getAndBitwiseOrRelease(where: C, value: V): V =
      self.getAndBitwiseOrRelease(where, value).asInstanceOf[V]

    @targetName("getAndVE")
    inline def getAndBitwiseAndVolatile(where: C, value: V): V =
      self.getAndBitwiseAnd(where, value).asInstanceOf[V]

    @targetName("getAndAE")
    inline def getAndBitwiseAndAcquire(where: C, value: V): V =
      self.getAndBitwiseAndAcquire(where, value).asInstanceOf[V]

    @targetName("getAndRE")
    inline def getAndBitwiseAndRelease(where: C, value: V): V =
      self.getAndBitwiseAndRelease(where, value).asInstanceOf[V]

    @targetName("getXorVE")
    inline def getAndBitwiseXorVolatile(where: C, value: V): V =
      self.getAndBitwiseXor(where, value).asInstanceOf[V]

    @targetName("getXorAE")
    inline def getAndBitwiseXorAcquire(where: C, value: V): V =
      self.getAndBitwiseXorAcquire(where, value).asInstanceOf[V]

    @targetName("getXorRE")
    inline def getAndBitwiseXorRelease(where: C, value: V): V =
      self.getAndBitwiseXorRelease(where, value).asInstanceOf[V]
    
  }
  
  extension[C, V <: AnyVal, N <: VarName] (self: VarHandle[C, V, N])
                                          (using ev: IntegralPrimitive[V], where: Magnet[C, V, N]) {

    @targetName("getOrVI")
    inline def getAndBitwiseOrVolatile(value: V): V =
      self.getAndBitwiseOr(where, value).asInstanceOf[V]

    @targetName("getOrAI")
    inline def getAndBitwiseOrAcquire(value: V): V =
      self.getAndBitwiseOrAcquire(where, value).asInstanceOf[V]

    @targetName("getOrRI")
    inline def getAndBitwiseOrRelease(value: V): V =
      self.getAndBitwiseOrRelease(where, value).asInstanceOf[V]

    @targetName("getAndVI")
    inline def getAndBitwiseAndVolatile(value: V): V =
      self.getAndBitwiseAnd(where, value).asInstanceOf[V]

    @targetName("getAndAI")
    inline def getAndBitwiseAndAcquire(value: V): V =
      self.getAndBitwiseAndAcquire(where, value).asInstanceOf[V]

    @targetName("getAndRI")
    inline def getAndBitwiseAndRelease(value: V): V =
      self.getAndBitwiseAndRelease(where, value).asInstanceOf[V]

    @targetName("getXorVI")
    inline def getAndBitwiseXorVolatile(value: V): V =
      self.getAndBitwiseXor(where, value).asInstanceOf[V]

    @targetName("getXorAI")
    inline def getAndBitwiseXorAcquire(value: V): V =
      self.getAndBitwiseXorAcquire(where, value).asInstanceOf[V]

    @targetName("getXorRI")
    inline def getAndBitwiseXorRelease(value: V): V =
      self.getAndBitwiseXorRelease(where, value).asInstanceOf[V]
    
  }

  import scala.reflect.*

  inline def apply[C]: Applicator[C] = new Applicator[C]

  class Applicator[C] {

    transparent inline def apply[V: ClassTag](inline getter: C => V)(using ClassTag[C]): Any =
      Macro.vhMacro[C, V](
        getter,
        classTag[C].runtimeClass.asInstanceOf[Class[C]],
        classTag[V].runtimeClass.asInstanceOf[Class[V]],
      )

  }

end VarHandle
export VarHandle.{VarGet, VarSet, VarHandle, Magnet}
package flames.concurrent.execution

import java.lang.invoke.VarHandle

object Fence:

  inline def full(): Unit =
    VarHandle.fullFence()

  inline def storeStore(): Unit =
    VarHandle.storeStoreFence()

  inline def loadLoad(): Unit =
    VarHandle.loadLoadFence()

  inline def acquire(): Unit =
    VarHandle.acquireFence()

  inline def release(): Unit =
    VarHandle.releaseFence()

end Fence

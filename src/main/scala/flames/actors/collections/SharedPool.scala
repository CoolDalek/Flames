package flames.actors.collections

import Pool.Reuse

class SharedPool[T: Reuse](
                            between: Int,
                            min: Int,
                            max: Int,
                            makeElement: () => T,
                            makePool: Pool.Factory
                          ) extends Pool[T] {

  private val underlyingPool = new ThreadLocal[Pool[T]] {
    override def get(): Pool[T] =
      val stored = super.get()
      if(stored == null)
        val local = makePool(
          min = min / between,
          max = max / between,
          make = makeElement,
        )
        set(local)
        local
      else stored
    end get
  }

  override def alloc(): T = underlyingPool.get().alloc()

  override def dealloc(used: T): Unit = underlyingPool.get().dealloc(used)

}

package flames.actors.fiber

import flames.actors.*
import flames.actors.path.*
import flames.actors.utils.*

trait Children {

  def add(ref: ErasedRef): Unit

  def remove(path: ActorPath): ErasedRef | Null

  def values: Set[ErasedRef]

  def search(by: ActorSelector): Set[ErasedRef]

}

object Children {
  import collection.mutable.Map as MutMap

  private trait Factory[Map[_, _]] {
    final type ByUnique = Map[Unique, ErasedRef]

    final type ByName = Map[String, ByUnique]

    def makeMap[K, V]: Map[K, V]

    def mkByName: ByName = makeMap[String, ByUnique]

    def mkByUnique: ByUnique = makeMap[Unique, ErasedRef]

    final val cachedEmpty: ByUnique = mkByUnique

  }

  private object Sync extends Factory[MutMap] {
    override def makeMap[K, V]: MutMap[K, V] = MutMap.empty
  }

  private object Async extends Factory[MutMap] {
    import scala.collection.concurrent.TrieMap

    override def makeMap[K, V]: TrieMap[K, V] = TrieMap.empty
  }

  private class ScalaMap(
    private val factory: Factory[MutMap],
  ) extends Children:
    private val underlying = factory.mkByName

    override def add(ref: ErasedRef): Unit =
      underlying.getOrElseUpdate(
        ref.path.name,
        factory.mkByUnique,
      ).addOne(
        ref.path.unique,
        ref,
      )

    override def remove(path: ActorPath): ErasedRef | Null =
      underlying.getOrElse(
        path.name,
        factory.cachedEmpty,
      ).remove(
        path.unique,
      ).orNull

    override def values: Set[ErasedRef] =
      underlying.values.flatMap(_.values) to Set

    override def search(by: ActorSelector): Set[ErasedRef] =
      val unique = underlying.getOrElse(
        by.name,
        factory.cachedEmpty,
      )
      by.unique.mapOrElse(
        x => unique.get(x) to Set,
        unique.values to Set,
      )
    end search

  end ScalaMap

  def sync: Children = ScalaMap(Sync)

  def async: Children = ScalaMap(Async)

}
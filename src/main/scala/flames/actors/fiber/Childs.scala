package flames.actors.fiber

import flames.actors.path.*
import flames.actors.*
import flames.actors.utils.*

trait Childs {

  def add(ref: ErasedRef): Unit

  def remove(path: ActorPath): ErasedRef | Null

  def values: Set[ErasedRef]

  def search(by: ActorSelector): Set[ErasedRef]

}
object Childs {

  import collection.mutable.Map as MutMap

  class ScalaMap(
                  private val factory: Factory[MutMap]
                ) extends Childs:

    private val underlying = factory.makeName

    override def add(ref: ErasedRef): Unit =
      underlying.getOrElseUpdate(
        ref.path.name,
        factory.makeUnique,
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

  trait Factory[Map[_, _]] {
    final type ByUnique = Map[Unique, ErasedRef]
    final type ByName = Map[String, ByUnique]

    def makeMap[K, V]: Map[K, V]

    def makeName: ByName = makeMap[String, ByUnique]

    def makeUnique: ByUnique = makeMap[Unique, ErasedRef]

    final val cachedEmpty: ByUnique = makeUnique

  }

  private object Sync extends Factory[MutMap] {
    override def makeMap[K, V]: MutMap[K, V] = MutMap.empty
  }
  
  def sync: Childs = ScalaMap(Sync)

  import scala.collection.concurrent.TrieMap

  private object Async extends Factory[MutMap] {
    override def makeMap[K, V]: TrieMap[K, V] = TrieMap.empty
  }
  
  def async: Childs = ScalaMap(Async)

}
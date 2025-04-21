package flames.actors.fiber

import flames.actors.*
import flames.actors.path.*
import flames.actors.utils.*

trait Children:

  def add(ref: ErasedRef): Unit

  def remove(path: ActorPath): ErasedRef | Null

  def values: Set[ErasedRef]

  def search(by: ActorSelector): Set[ErasedRef]

object Children:
  import scala.collection.concurrent.TrieMap
  import scala.collection.mutable

  private trait ScalaMap extends Children:
    final type ByUnique = mutable.Map[Unique, ErasedRef]

    final type ByName = mutable.Map[String, ByUnique]

    protected def makeMap[K, V]: mutable.Map[K, V]

    private def mkByName: ByName = makeMap[String, ByUnique]

    private def mkByUnique: ByUnique = makeMap[Unique, ErasedRef]

    private val underlying = mkByName

    override def add(ref: ErasedRef): Unit =
      underlying.getOrElseUpdate(
        ref.path.name,
        mkByUnique,
      ).addOne(
        ref.path.unique,
        ref,
      )

    override def remove(path: ActorPath): ErasedRef | Null =
      underlying.getOrElse(
        path.name,
        mkByUnique,
      ).remove(
        path.unique,
      ).orNull

    override def values: Set[ErasedRef] =
      underlying.values.flatMap(_.values) to Set

    override def search(by: ActorSelector): Set[ErasedRef] =
      val unique = underlying.getOrElse(
        by.name,
        mkByUnique,
      )
      by.unique.mapOrElse(
        x => unique.get(x) to Set,
        unique.values to Set,
      )
    end search

  end ScalaMap

  def sync: Children = new ScalaMap {
    override protected def makeMap[K, V]: mutable.Map[K, V] = mutable.Map.empty
  }

  def async: Children = new ScalaMap {
    override protected def makeMap[K, V]: mutable.Map[K, V] = TrieMap.empty
  }

end Children

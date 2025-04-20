package flames.actors.path

import java.util.UUID
import scala.reflect.*

trait Unique {

  private[actors] def next(): Unique

  def value: String

  final override def toString: String = value

}
object Unique {

  private trait Delegate[From: Typeable, To](using Typeable[Delegate[?, ?]]) extends Unique:
    this: To =>
    protected val self: From

    final override def hashCode(): Int = self.##

    final override def value: String = self.toString

    final override def equals(obj: Any): Boolean =
      obj match
        case that: Delegate[?, ?] =>
          that.self match
            case other: From =>
              other == self
            case _ => false
        case _ => false
    end equals

  end Delegate

  private class Increment(
    val self: Long,
    var children: Long,
  ) extends Delegate[Long, Increment]:

    override def next(): Unique = {
      children += 1
      Increment(children, 0)
    }

  end Increment

  def increment(): Unique = Increment(0, 0)

  private class Uuid(
    val self: UUID,
    val factory: () => UUID,
  ) extends Delegate[UUID, Uuid] {
    def next(): Uuid = Uuid(factory(), factory)
  }

  def uuid(factory: => UUID = UUID.randomUUID()): Unique = Uuid(factory, () => factory)

  private class Timestamp(
    val self: Long,
    val factory: () => Long,
  ) extends Delegate[Long, Timestamp] {
    def next(): Unique = Timestamp(factory(), factory)
  }

  def timestamp(factory: => Long = System.nanoTime()): Unique = Timestamp(factory, () => factory)

}
package flames.ui

import flames.concurrent.actor.*

import java.util.concurrent.atomic.AtomicReference

trait AsyncScreen[T, Type <: ActorType] extends Screen with Actor[T, Type] {
  type State
  protected def initialState: State
  private lazy val state = AtomicReference(initialState)

  sealed trait Get
  sealed trait Set extends Get

  inline protected def set(inline value: State)(using Set): Unit =
    state.set(value)

  inline protected def get()(using Get): State =
    state.get()

  inline protected def modify(inline action: State => State)(using Set): Unit =
    state.getAndUpdate(s => action(s))

  final override def render(delta: Float): Unit = {
    given Get = new Get {}
    view(delta)
  }

  def view(delta: Float)(using Get): Unit

  final override def act(): Behavior[T] = {
    given Set = new Set {}
    update()
  }

  def update()(using Set): Behavior[T]

}
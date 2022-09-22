package flames.actors

type WithState[T] = StateAccess ?=> T
sealed trait StateAccess
private[actors] object StateAccess extends StateAccess {

  inline given StateAccess = this

}

package flames.concurrent.actor.mailbox

trait Mailbox[+T] {

  def isEmpty: Boolean

  def poll(): T | Null

}

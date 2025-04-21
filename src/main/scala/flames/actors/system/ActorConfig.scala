package flames.actors.system

import flames.actors.fiber.Children
import flames.actors.message.Mailbox

case class ActorConfig(
  autoYield: Int,
  mkMailbox: Mailbox.Make,
  mkChildren: () => Children,
)
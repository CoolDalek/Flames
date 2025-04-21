package flames.actors.fiber

import flames.actors.message.StopReason

enum State:
  case Idle extends State
  case Running extends State
  case Stopped(reason: StopReason) extends State

package flames.actors.fiber

enum State {
  case Idle extends State
  case Running extends State
  case Stopped extends State
}
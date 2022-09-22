package flames.actors.fiber

enum ReceiveResult {
  case EmptyQueue
  case Continue
  case Break
}

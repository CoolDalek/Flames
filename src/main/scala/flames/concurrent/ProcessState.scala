package flames.concurrent

enum ProcessState {
  case Stop extends ProcessState
  case Running extends ProcessState
  case Idle extends ProcessState
}
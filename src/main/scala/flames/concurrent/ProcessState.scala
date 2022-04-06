package flames.concurrent

import ProcessState.*

type ProcessState = Stop | Running | Idle
object ProcessState {
  type Stop = 1
  val Stop: Stop = 1
  type Running = 2
  val Running: Running = 2
  type Idle = 3
  val Idle: Idle = 3
}
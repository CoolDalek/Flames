package flames.ui

import flames.logging.FailureReporter

case class LauncherConfig(
                           title: String,
                           size: WindowSize,
                           renderer: Renderer,
                           failureReporter: FailureReporter,
                         ) extends FailureReporter {
  export failureReporter.reportFailure
}
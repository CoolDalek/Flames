package flames.ui

import flames.logging.FailureReporter
import org.jetbrains.skija.Canvas

class UIContext(
                 val router: Router,
                 val canvas: Canvas,
                 reporter: FailureReporter,
               ) extends FailureReporter {
  export reporter.reportFailure
}
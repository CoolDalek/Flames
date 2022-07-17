package flames.ui

import flames.logging.FailureReporter
import org.jetbrains.skija.Canvas

final class App private(
                         initialScreen: Route[Screen],
                         reporter: FailureReporter,
                         canvas: Canvas,
                       ) extends UIListener {

  private object AppRouter extends Router {

    override def moveTo[T <: Screen : Route]: Unit = {
      current.close()
      current = Route.make[T](context)
    }

  }

  private val context: UIContext =
    UIContext(
      AppRouter,
      canvas,
      reporter,
    )

  private var current: Screen = initialScreen.make(context)

  final override def resize(width: Int, height: Int): Unit =
    current.resize(width, height)

  final override def render(delta: Float): Unit =
    current.render(delta)

  final override def pause(): Unit =
    current.pause()

  final override def resume(): Unit =
    current.resume()

  final override def close(): Unit =
    current.close()

}
object App {

  def apply[T <: Screen : Route]: (FailureReporter, Canvas) => App =
    (reporter, canvas) => new App(Route.erased[T], reporter, canvas)

}
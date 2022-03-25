package flames.ui

import flames.util.FailureReporter
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

  override final def resize(width: Int, height: Int): Unit =
    current.resize(width, height)

  override final def render(delta: Float): Unit =
    current.render(delta)

  override final def pause(): Unit =
    current.pause()

  override final def resume(): Unit =
    current.resume()

  override final def close(): Unit =
    current.close()

}
object App {

  def apply[T <: Screen : Route]: (FailureReporter, Canvas) => App =
    (reporter, canvas) => new App(Route.erased[T], reporter, canvas)

}
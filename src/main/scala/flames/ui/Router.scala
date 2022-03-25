package flames.ui

import scala.reflect.ClassTag

trait Router {

  def moveTo[T <: Screen: Route]: Unit

}
package flames.ui

import scala.compiletime.*

sealed trait Renderer
object Renderer {

  case class OpenGL private(major: Int, minor: Int) extends Renderer
  object OpenGL {

    private def make(maj: Int, min: Int): OpenGL = new OpenGL(maj, min)

    inline def apply(inline maj: Int, inline min: Int): OpenGL = {
      inline val maxMinor = inline maj match {
        case 1 => 5
        case 2 => 1
        case 3 => 3
        case 4 => 6
        case _ => error("Invalid major OpenGL version")
      }
      inline if(min < 0 || min > maxMinor) {
        error("Invalid minor OpenGL version")
      } else {
        make(maj, min)
      }
    }
  }

  //TODO: Vulkan

}
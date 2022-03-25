package flames.ui

import scala.compiletime.*

sealed trait Renderer
object Renderer {

  case class OpenGL private(major: Int, minor: Int) extends Renderer
  object OpenGL {

    inline def apply[Major <: Int: ValueOf, Minor <: Int: ValueOf]: OpenGL = {
      val maj = valueOf[Major]
      val min = valueOf[Minor]
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
        OpenGL(maj, min)
      }
    }
  }

  //TODO: Vulkan

}
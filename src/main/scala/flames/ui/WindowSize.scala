package flames.ui

enum WindowSize {

  inline final val DontCare = org.lwjgl.glfw.GLFW.GLFW_DONT_CARE

  case Fullscreen extends WindowSize
  case Maximized extends WindowSize
  case Limited(
                minWidth: Int,
                minHeight: Int,
                maxWidth: Int,
                maxHeight: Int,
                widthRatio: Int,
                heightRatio: Int,
              ) extends WindowSize
  case Ordinary(width: Int, height: Int, resizeable: Boolean) extends WindowSize

}
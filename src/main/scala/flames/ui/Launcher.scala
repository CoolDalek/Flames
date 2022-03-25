package flames.ui

import flames.ui.WindowSize.*
import flames.util.FailureReporter
import org.jetbrains.skija.*
import org.jetbrains.skija.impl.Library as SkjaLib
import org.lwjgl.glfw.*
import org.lwjgl.glfw.Callbacks.*
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.*
import org.lwjgl.system.MemoryUtil.*
import org.lwjgl.system.*

import java.io.PrintStream
import java.util
import scala.util.control.NonFatal

object Launcher {

  def run(config: LauncherConfig, factory: (FailureReporter, Canvas) => UIListener): Unit = {

    new GLFWErrorCallback() {
      private val errorCodes: util.Map[Integer, String] =
        org.lwjgl.system.APIUtil.apiClassTokens(
          (field, value) => 0x10000 < value && value < 0x20000,
          null,
          classOf[GLFW],
        )
      override def invoke(error: Int, description: Long): Unit = {
        val exc = LwjglError(
          errorCodes.get(error),
          GLFWErrorCallback.getDescription(description),
        )
        config.failureReporter.reportFailure(exc)
      }
    }.set()

    if(!glfwInit()) throw IllegalStateException("Cannot init GLFW")

    config.renderer match {
      case Renderer.OpenGL(major, minor) =>
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, major)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, minor)
    }

    val stencilBits = 0
    glfwWindowHint(GLFW_STENCIL_BITS, stencilBits)
    glfwWindowHint(GLFW_DEPTH_BITS, 0)

    val window = config.size match {
      case Fullscreen =>
        glfwCreateWindow(
          GLFW_DONT_CARE,
          GLFW_DONT_CARE,
          config.title,
          glfwGetPrimaryMonitor(),
          NULL,
        )
      case Maximized =>
        glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE)
        glfwCreateWindow(
          GLFW_DONT_CARE,
          GLFW_DONT_CARE,
          config.title,
          NULL,
          NULL,
        )
      case Limited(minWidth, minHeight, maxWidth, maxHeight, widthRatio, heightRatio) =>
        val window = glfwCreateWindow(
          minWidth,
          minHeight,
          config.title,
          NULL,
          NULL,
        )
        glfwSetWindowSizeLimits(window, minWidth, minHeight, maxWidth, maxHeight)
        glfwSetWindowAspectRatio(window, widthRatio, heightRatio)
        window
      case Ordinary(width, height, resizeable) =>
        val glfwResizable = if(resizeable) GLFW_TRUE else GLFW_FALSE
        glfwWindowHint(GLFW_RESIZABLE, glfwResizable)
        glfwCreateWindow(
          width,
          height,
          config.title,
          NULL,
          NULL,
        )
    }

    if(window == NULL) throw RuntimeException("Failed to create GLFW window")

    try {

      glfwMakeContextCurrent(window)
      glfwSwapInterval(1)
      GL.createCapabilities()

      if(sys.props.get("skija.staticload").contains("false"))
        SkjaLib.load()
      val context = DirectContext.makeGL()

      val stack = MemoryStack.stackPush()
      val width = stack.mallocInt(1)
      val height = stack.mallocInt(1)

      glfwGetFramebufferSize(window, width, height)

      val renderTarget = BackendRenderTarget.makeGL(
        width.get(0),
        height.get(0),
        0,
        stencilBits,
        0,
        FramebufferFormat.GR_GL_RGBA8,
      )
      val surface = Surface.makeFromBackendRenderTarget(
        context,
        renderTarget,
        SurfaceOrigin.BOTTOM_LEFT,
        SurfaceColorFormat.RGBA_8888,
        ColorSpace.getSRGB,
      )
      val canvas = surface.getCanvas
      val app = factory(config.failureReporter, canvas)

      glfwShowWindow(window)

      new GLFWFramebufferSizeCallback() {
        override def invoke(window: Long, width: Int, height: Int): Unit = app.resize(width, height)
      }.set(window)

      new GLFWWindowFocusCallback() {
        override def invoke(window: Long, focused: Boolean): Unit =
          if(focused) app.resume()
          else app.pause()
      }.set(window)

      try {
        var lastFrame = System.nanoTime()
        var delta = 0f
        while(!glfwWindowShouldClose(window)) {
          val current = System.nanoTime()
          delta = (current - lastFrame) / 1000000000f
          lastFrame = current
          app.render(delta)
          context.flush()
          glfwSwapBuffers(window)
          glfwPollEvents()
        }
      } finally {
        app.close()
        surface.close()
        renderTarget.close()
        context.close()
      }

    } catch {
      case NonFatal(exc) =>
        exc.printStackTrace()
    } finally {
      glfwFreeCallbacks(window)
      glfwDestroyWindow(window)
      glfwTerminate()
      val reporter = glfwSetErrorCallback(null)
      if(reporter != null) reporter.free()
    }

  }

}
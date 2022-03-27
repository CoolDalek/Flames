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
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.control.NonFatal

object Launcher {

  private val toClose: mutable.Stack[AutoCloseable] = mutable.Stack.empty[AutoCloseable]

  inline private def use[T <: AutoCloseable](inline value: T): T = {
    toClose.push(value)
    value
  }

  private def initGlfw(config: LauncherConfig): Unit = {
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
  }

  inline private val stencilBits = 0

  private def makeWindow(config: LauncherConfig): Long = {
    config.renderer match {
      case Renderer.OpenGL(major, minor) =>
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, major)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, minor)
    }

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
    window
  }

  private def makeContext(window: Long): DirectContext = {
    glfwMakeContextCurrent(window)
    glfwSwapInterval(1)
    GL.createCapabilities()

    if(sys.props.get("skija.staticload").contains("false"))
      SkjaLib.load()
    use(DirectContext.makeGL())
  }

  private def makeCanvas(window: Long, context: DirectContext): Canvas = {
    val stack = MemoryStack.stackPush()
    val width = stack.mallocInt(1)
    val height = stack.mallocInt(1)

    glfwGetFramebufferSize(window, width, height)

    val renderTarget = use {
      BackendRenderTarget.makeGL(
        width.get(0),
        height.get(0),
        0,
        stencilBits,
        0,
        FramebufferFormat.GR_GL_RGBA8,
      )
    }
    val surface = use {
      Surface.makeFromBackendRenderTarget(
        context,
        renderTarget,
        SurfaceOrigin.BOTTOM_LEFT,
        SurfaceColorFormat.RGBA_8888,
        ColorSpace.getSRGB,
      )
    }
    use(surface.getCanvas)
  }

  def run(config: LauncherConfig, makeApp: (FailureReporter, Canvas) => UIListener): Unit = {

    initGlfw(config)
    val window = makeWindow(config)

    try {
      val context = makeContext(window)
      val canvas = makeCanvas(window, context)
      val app = use {
        makeApp(config.failureReporter, canvas)
      }

      glfwShowWindow(window)

      try {

        new GLFWFramebufferSizeCallback() {
          override def invoke(window: Long, width: Int, height: Int): Unit = app.resize(width, height)
        }.set(window)

        var focused = true
        var lastFrame = System.nanoTime()
        var delta = 0f

        new GLFWWindowFocusCallback() {
          override def invoke(window: Long, signal: Boolean): Unit = {
            focused = signal
            if (focused) {
              lastFrame = System.nanoTime()
              delta = 0f
              app.resume()
            }
            else {
              app.pause()
            }
          }
        }.set(window)

        while(!glfwWindowShouldClose(window)) {
          if(focused) {
            val current = System.nanoTime()
            delta = (current - lastFrame) / 1000000000f
            lastFrame = current
            app.render(delta)
            context.flush()
            glfwSwapBuffers(window)
            glfwPollEvents()
          } else {
            glfwWaitEvents()
          }
        }
      } finally {
        toClose.foreach(_.close())
      }

    } catch {
      case NonFatal(exc) =>
        config.failureReporter.reportFailure(exc)
    } finally {
      glfwFreeCallbacks(window)
      glfwDestroyWindow(window)
      glfwTerminate()
      val reporter = glfwSetErrorCallback(null)
      if(reporter != null) reporter.free()
    }

  }

}
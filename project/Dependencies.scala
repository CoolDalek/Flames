import sbt._

object Dependencies {

  private val linux = "linux"
  private val windows = "windows"
  private val macos = "macos"
  private def x86(os: String) = s"$os-x86"
  private def x64(os: String) = s"$os-x64"
  private def arm32(os: String) = s"$os-arm32"
  private def arm64(os: String) = s"$os-arm64"

  def resolvers = Seq(
    "space-maven" at "https://packages.jetbrains.team/maven/p/skija/maven",
  )

  object JOML {

    def apply(): ModuleID = "org.joml" % "joml" % "1.10.4"

  }

  object JBox2D {

    def apply(): ModuleID = "org.jbox2d" % "jbox2d-library" % "2.2.1.1"

  }

  object Lwjgl {

    def apply(): Seq[ModuleID] = {
      val core = ""
      val jvm = ""

      def dependency(platform: String, lib: String): ModuleID =
        "org.lwjgl" % {
          val suffix = if(lib == core) lib else s"-$lib"
          s"lwjgl$suffix"
        } % "3.3.1" classifier {
          if(platform == jvm) platform else s"natives-$platform"
        }

      val libs = Seq(
        core,
        "glfw",
        "opengl",
      )
      val platforms = Seq(
        jvm,
        linux,
        arm32(linux),
        arm64(linux),
        macos,
        arm64(macos),
        windows,
        x86(windows),
      )
      for {
        lib <- libs
        platform <- platforms
      } yield dependency(platform, lib)
    }

  }

  object Skija {

    def apply(): Seq[ModuleID] = {
      val skija = "skija"
      def jetbrains = "org.jetbrains"
      val org = s"$jetbrains.$skija"
      val shared = "shared"
      def internal(platform: String): ModuleID =
        org % s"$skija-$platform" % "0.93.1"
      def external(platform: String): ModuleID =
        internal(platform) exclude(org, s"$skija-$shared")
      val platforms = Seq(
        x64(macos),
        arm64(macos),
        linux,
        windows,
      )
      platforms.map(external) :+ internal(shared) :+ {
        jetbrains % "annotations" % "20.1.0" % Provided
      }
    }

  }

}
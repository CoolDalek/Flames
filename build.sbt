ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.2.1"

lazy val root = (project in file("."))
  .settings(
    name := "Flames",
    libraryDependencies ++= Seq(
      "org.jctools" % "jctools-core" % "3.3.0",
      "org.typelevel" %% "cats-effect" % "3.3.14",
      "dev.zio" %% "zio" % "2.0.2",
    )
  )

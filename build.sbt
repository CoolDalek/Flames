ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.2.0"

lazy val root = (project in file("."))
  .settings(
    name := "Flames",
    libraryDependencies ++= Seq(
      "org.jctools" % "jctools-core" % "4.0.1",
    )
  )

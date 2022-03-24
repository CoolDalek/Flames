ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.1.1"

lazy val root = (project in file("."))
  .settings(
    name := "Flames",
    libraryDependencies ++= Seq(
      "org.joml" % "joml" % "1.10.4",
      "org.jbox2d" % "jbox2d-library" % "2.2.1.1",
    ),
  )
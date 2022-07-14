ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.1.2"

lazy val root = (project in file("."))
  .settings(
    name := "Flames",
    resolvers ++= Dependencies.resolvers,
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "sourcecode" % "0.2.8",
      "org.jctools" % "jctools-core" % "3.3.0",
      Dependencies.JBox2D(),
      Dependencies.JOML(),
      "org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.4",
    ) ++ Dependencies.Lwjgl() ++ Dependencies.Skija(),
  )
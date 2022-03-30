ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.1.1"

lazy val root = (project in file("."))
  .settings(
    name := "Flames",
    resolvers ++= Dependencies.resolvers,
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "sourcecode" % "0.2.8",
      Dependencies.JBox2D(),
      Dependencies.JOML(),
    ) ++ Dependencies.Lwjgl() ++ Dependencies.Skija(),
  )
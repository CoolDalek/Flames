ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.2.0"

lazy val root = (project in file("."))
  .settings(
    name := "Flames",
    libraryDependencies ++= Seq(
      "dev.zio" % "zio-actors_2.13" % "0.0.9",
      "com.typesafe.akka" %% "akka-actor-typed" % "2.6.20",
      "com.lihaoyi" % "castor_2.13" % "0.1.7",
    )
  )

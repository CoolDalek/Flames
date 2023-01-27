ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.2.2"

lazy val root = (project in file("."))
  .settings(
    name := "Flames",
    libraryDependencies ++= Seq(
      "org.jctools" % "jctools-core" % "4.0.1",
      "org.typelevel" %% "cats-effect" % "3.4.5",
      "dev.zio" %% "zio" % "2.0.6",
      "org.scalatest" %% "scalatest" % "3.2.15" % Test,
      "org.scalatestplus" %% "scalacheck-1-17" % "3.2.15.0" % Test,
      "org.scalacheck" %% "scalacheck" % "1.17.0" % Test,
    ),
    scalacOptions ++= Seq(
      "-explain",
      "-explain-types",
      "-deprecation",
      "-source:future",
    ),
  )

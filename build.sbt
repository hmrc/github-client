import sbt._

val appName = "github-client"

lazy val library = Project(appName, file("."))
  .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning, SbtArtifactory)
  .settings(
    majorVersion := 3,
    scalaVersion := "2.12.12",
    makePublicallyAvailableOnBintray := true,
    libraryDependencies ++= LibDependencies.compile ++ LibDependencies.test
  )

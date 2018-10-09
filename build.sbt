import sbt._

val appName = "github-client"

lazy val library = Project(appName, file("."))
  .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning, SbtArtifactory)
  .settings(
    majorVersion := 2,
    makePublicallyAvailableOnBintray := true,
    libraryDependencies ++= LibDependencies.compile ++ LibDependencies.test
  )

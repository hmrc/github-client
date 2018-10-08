import sbt._

val appName = "github-client"

lazy val library = Project(appName, file("."))
  .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning, SbtArtifactory)
  .settings(
    majorVersion := 1,
    makePublicallyAvailableOnBintray := true,
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test
  )

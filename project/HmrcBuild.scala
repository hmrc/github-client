import sbt.Keys._
import sbt.Tests.{SubProcess, Group}
import sbt._
import uk.gov.hmrc.SbtAutoBuildPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning

object HmrcBuild extends Build {


  val appName = "github-client"

  lazy val library = Project(appName, file("."))
    .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning)
    .settings(scalaVersion := "2.11.6")
    .settings(
      parallelExecution in Test := false,
      fork in Test := false,
      retrieveManaged := true
    )
    .settings(libraryDependencies ++= AppDependencies())
}

private object AppDependencies {

  val compile = Seq(
    "com.github.scopt" %% "scopt" % "3.3.0",
    "com.typesafe.play" %% "play-ws" % "2.3.10",
    "commons-io" % "commons-io" % "2.4",
    "org.apache.httpcomponents" % "httpcore" % "4.3.2",
    "org.apache.httpcomponents" % "httpclient" % "4.3.5",
    "io.spray" %% "spray-json" % "1.3.2",
    "org.eclipse.mylyn.github" % "org.eclipse.egit.github.core" % "2.1.5"
  )


  lazy val test: Seq[ModuleID] = Seq(
    "org.scalatest" %% "scalatest" % "2.2.4" % "test",
    "org.pegdown" % "pegdown" % "1.4.2" % "test",
    "org.mockito" % "mockito-all" % "1.9.5" % "test",
    "com.github.tomakehurst" % "wiremock" % "1.52" % "test"
  )

  def apply() = compile ++ test
}



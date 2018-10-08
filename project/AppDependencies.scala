import sbt._

private object AppDependencies {

  val compile: Seq[ModuleID] = Seq(
    "com.github.scopt"          %% "scopt"                       % "3.3.0",
    "com.typesafe.play"         %% "play-ws"                     % "2.3.10",
    "commons-io"                % "commons-io"                   % "2.4",
    "org.apache.httpcomponents" % "httpcore"                     % "4.3.2",
    "org.apache.httpcomponents" % "httpclient"                   % "4.3.5",
    "io.spray"                  %% "spray-json"                  % "1.3.2",
    "org.eclipse.mylyn.github"  % "org.eclipse.egit.github.core" % "2.1.5"
  )

  val test: Seq[ModuleID] = Seq(
    "org.scalatest"          %% "scalatest"  % "2.2.4" % Test,
    "org.pegdown"            % "pegdown"     % "1.4.2" % Test,
    "org.mockito"            % "mockito-all" % "1.9.5" % Test,
    "com.github.tomakehurst" % "wiremock"    % "1.52"  % Test
  )
}

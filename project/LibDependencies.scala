import sbt._

private object LibDependencies {

  val compile: Seq[ModuleID] = Seq(
    "org.eclipse.mylyn.github" %  "org.eclipse.egit.github.core" % "2.1.5",
    "com.typesafe.play"        %% "play-json"                    % "2.8.1",
    "org.slf4j"                %  "slf4j-api"                    % "1.7.30",
    "com.typesafe"             %  "config"                       % "1.4.1",
    "com.google.guava"         %  "guava"                        % "28.2-jre" // TODO find a lightweight alternative, we only need Base64Encoding
  )

  val test: Seq[ModuleID] = Seq(
    "org.scalatest"        %% "scalatest"     % "3.2.3"   % Test,
    "com.vladsch.flexmark" %  "flexmark-all"  % "0.35.10" % Test,
    "org.mockito"          %% "mockito-scala" % "1.10.1"  % Test
  )
}

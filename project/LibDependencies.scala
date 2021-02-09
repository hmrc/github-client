import sbt._

private object LibDependencies {

  val compile: Seq[ModuleID] = Seq(
    "com.github.scopt"         %% "scopt"                        % "4.0.0",
    "com.typesafe.play"        %% "play-ws"                      % "2.8.7",
    "org.eclipse.mylyn.github" %  "org.eclipse.egit.github.core" % "2.1.5"
  )

  val test: Seq[ModuleID] = Seq(
    "org.scalatest"        %% "scalatest"     % "3.2.3"   % Test,
    "com.vladsch.flexmark" %  "flexmark-all"  % "0.35.10" % Test,
    "org.mockito"          %% "mockito-scala" % "1.10.1"  % Test
  )
}

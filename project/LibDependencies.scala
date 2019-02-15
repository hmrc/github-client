import sbt._

private object LibDependencies {

  val compile: Seq[ModuleID] = Seq(
    "com.github.scopt"         %% "scopt"                       % "3.7.0",
    "com.typesafe.play"        %% "play-ws"                     % "2.6.19",
    "org.eclipse.mylyn.github" % "org.eclipse.egit.github.core" % "2.1.5",
    // force dependencies due to security flaws found in jackson-databind < 2.9.x using XRay
    "com.fasterxml.jackson.core"     % "jackson-core"            % "2.9.7",
    "com.fasterxml.jackson.core"     % "jackson-databind"        % "2.9.7",
    "com.fasterxml.jackson.core"     % "jackson-annotations"     % "2.9.7",
    "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk8"   % "2.9.7",
    "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % "2.9.7"
  )

  val test: Seq[ModuleID] = Seq(
    "org.mockito"   % "mockito-core" % "1.9.5" % Test,
    "org.pegdown"   % "pegdown"      % "1.6.0" % Test,
    "org.scalamock" %% "scalamock"   % "4.1.0" % Test,
    "org.scalatest" %% "scalatest"   % "3.0.5" % Test
  )
}

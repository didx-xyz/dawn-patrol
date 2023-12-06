scalaVersion := "3.3.1"

lazy val root = (project in file("."))
  .settings(
    settings,
    libraryDependencies ++= Seq(
      "io.circe"                      %% "circe-core"                     % circeVersion,
      "io.circe"                      %% "circe-parser"                   % circeVersion,
      "io.circe"                      %% "circe-generic"                  % circeVersion,
      "com.github.pureconfig"         %% "pureconfig-core"                % pureconfigVersion,
      "com.github.pureconfig"         %% "pureconfig-cats-effect"         % pureconfigVersion,
      "dev.profunktor"                %% "redis4cats-effects"             % redis4CatsVersion,
      "dev.profunktor"                %% "redis4cats-log4cats"            % redis4CatsVersion,
      "com.softwaremill.sttp.client4" %% "core"                           % "4.0.0-M6",
      "com.softwaremill.sttp.client3" %% "core"                           % sttpClient3Version,
      "com.softwaremill.sttp.client3" %% "circe"                          % sttpClient3Version,
      "com.softwaremill.sttp.client3" %% "async-http-client-backend-cats" % sttpClient3Version,
      "de.brendamour"                  % "jpasskit"                       % "0.3.3",
      "com.outr"                      %% "scribe"                         % "3.12.2",
      "com.lihaoyi"                   %% "upickle"                        % "3.1.3",
      "dev.langchain4j"                % "langchain4j"                    % langchain4jVersion,
      "dev.langchain4j"                % "langchain4j-hugging-face"       % langchain4jVersion,
      "com.xebia"                     %% "xef-scala"                      % xefVersion,
      "com.xebia"                      % "xef-pdf"                        % xefVersion,
      "com.xebia"                      % "xef-reasoning-jvm"              % xefVersion,
      "com.xebia"                      % "xef-openai"                     % xefVersion,
      "ch.qos.logback"                 % "logback-classic"                % "1.4.11",
      "org.apache.logging.log4j"       % "log4j-core"                     % "2.21.1",
      "org.typelevel"                 %% "log4cats-slf4j"                 % "2.6.0",
      "co.fs2"                        %% "fs2-core"                       % "3.9.3"
    )
  )

lazy val circeVersion       = "0.14.6"
lazy val pureconfigVersion  = "0.17.4"
lazy val redis4CatsVersion  = "1.5.2"
lazy val sttpClient3Version = "3.9.1"
lazy val langchain4jVersion = "0.23.0"
lazy val xefVersion         = "0.0.3"

lazy val compilerOptions = Seq("-Xmax-inlines", "50")

lazy val commonSettings = Seq(
  scalacOptions ++= compilerOptions
)

lazy val scalafixSettings = Seq(semanticdbEnabled := true)

lazy val settings = commonSettings ++ scalafixSettings

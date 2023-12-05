scalaVersion := "3.3.1"

scalacOptions ++= Seq("-Xmax-inlines", "50")

val circeVersion       = "0.14.6"
val pureconfigVersion  = "0.17.4"
val profunktorVersion  = "1.5.2"
val client3Version     = "3.9.1"
val langchain4jVersion = "0.23.0"
val xebiaVersion       = "0.0.3"

libraryDependencies ++= Seq(
  "io.circe"                      %% "circe-core"                     % circeVersion,
  "io.circe"                      %% "circe-parser"                   % circeVersion,
  "io.circe"                      %% "circe-generic"                  % circeVersion,
  "com.github.pureconfig"         %% "pureconfig-core"                % pureconfigVersion,
  "com.github.pureconfig"         %% "pureconfig-cats-effect"         % pureconfigVersion,
  "dev.profunktor"                %% "redis4cats-effects"             % profunktorVersion,
  "dev.profunktor"                %% "redis4cats-log4cats"            % profunktorVersion,
  "com.softwaremill.sttp.client4" %% "core"                           % "4.0.0-M6",
  "com.softwaremill.sttp.client3" %% "core"                           % client3Version,
  "com.softwaremill.sttp.client3" %% "circe"                          % client3Version,
  "com.softwaremill.sttp.client3" %% "async-http-client-backend-cats" % client3Version,
  "de.brendamour"                  % "jpasskit"                       % "0.3.3",
  "com.outr"                      %% "scribe"                         % "3.12.2",
  "com.lihaoyi"                   %% "upickle"                        % "3.1.3",
  "dev.langchain4j"                % "langchain4j"                    % langchain4jVersion,
  "dev.langchain4j"                % "langchain4j-hugging-face"       % langchain4jVersion,
  "com.xebia"                     %% "xef-scala"                      % xebiaVersion,
  "com.xebia"                      % "xef-pdf"                        % xebiaVersion,
  "com.xebia"                      % "xef-reasoning-jvm"              % xebiaVersion,
  "com.xebia"                      % "xef-openai"                     % xebiaVersion,
  "ch.qos.logback"                 % "logback-classic"                % "1.4.11",
  "org.apache.logging.log4j"       % "log4j-core"                     % "2.21.1",
  "org.typelevel"                 %% "log4cats-slf4j"                 % "2.6.0",
  "co.fs2"                        %% "fs2-core"                       % "3.9.3"
)

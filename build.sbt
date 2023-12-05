scalaVersion := "3.3.1"

scalacOptions ++= Seq("-Xmax-inlines", "50")

Compile / mainClass := Some("xyz.didx.DawnPatrol")

testFrameworks += new TestFramework("munit.Framework")

libraryDependencies ++= Seq(
  "io.circe"                      %% "circe-core"                     % "0.14.6",
  "io.circe"                      %% "circe-parser"                   % "0.14.6",
  "io.circe"                      %% "circe-generic"                  % "0.14.6",
  "com.github.pureconfig"         %% "pureconfig-core"                % "0.17.4",
  "com.github.pureconfig"         %% "pureconfig-cats-effect"         % "0.17.4",
  "dev.profunktor"                %% "redis4cats-effects"             % "1.5.2",
  "dev.profunktor"                %% "redis4cats-log4cats"            % "1.5.2",
  "com.softwaremill.sttp.client4" %% "core"                           % "4.0.0-M6",
  "com.softwaremill.sttp.client3" %% "core"                           % "3.9.1",
  "com.softwaremill.sttp.client3" %% "circe"                          % "3.9.1",
  "com.softwaremill.sttp.client3" %% "async-http-client-backend-cats" % "3.9.1",
  "de.brendamour"                  % "jpasskit"                       % "0.3.3",
  "com.outr"                      %% "scribe"                         % "3.12.2",
  "com.lihaoyi"                   %% "upickle"                        % "3.1.3",
  "dev.langchain4j"                % "langchain4j"                    % "0.23.0",
  "dev.langchain4j"                % "langchain4j-hugging-face"       % "0.23.0",
  "com.xebia"                     %% "xef-scala"                      % "0.0.3",
  "com.xebia"                      % "xef-pdf"                        % "0.0.3",
  "com.xebia"                      % "xef-reasoning-jvm"              % "0.0.3",
  "com.xebia"                      % "xef-openai"                     % "0.0.3",
  "ch.qos.logback"                 % "logback-classic"                % "1.4.11",
  "org.apache.logging.log4j"       % "log4j-core"                     % "2.21.1",
  "org.typelevel"                 %% "log4cats-slf4j"                 % "2.6.0",
  "co.fs2"                        %% "fs2-core"                       % "3.9.3"
)

libraryDependencies ++= Seq(
  "org.typelevel"                 %% "toolkit-test"                   % "latest.release" % Test,
  "io.circe"                      %% "circe-core"                     % "0.14.6"         % Test,
  "io.circe"                      %% "circe-parser"                   % "0.14.6"         % Test,
  "io.circe"                      %% "circe-generic"                  % "0.14.6"         % Test,
  "com.github.pureconfig"         %% "pureconfig-core"                % "0.17.4"         % Test,
  "com.github.pureconfig"         %% "pureconfig-cats-effect"         % "0.17.4"         % Test,
  "dev.profunktor"                %% "redis4cats-effects"             % "1.5.2"          % Test,
  "dev.profunktor"                %% "redis4cats-log4cats"            % "1.5.2"          % Test,
  "com.softwaremill.sttp.client4" %% "core"                           % "4.0.0-M6"       % Test,
  "com.softwaremill.sttp.client3" %% "core"                           % "3.9.1"          % Test,
  "com.softwaremill.sttp.client3" %% "circe"                          % "3.9.1"          % Test,
  "com.softwaremill.sttp.client3" %% "async-http-client-backend-cats" % "3.9.1"          % Test,
  "de.brendamour"                  % "jpasskit"                       % "0.3.3"          % Test,
  "com.outr"                      %% "scribe"                         % "3.12.2"         % Test,
  "com.lihaoyi"                   %% "upickle"                        % "3.1.3"          % Test,
  "dev.langchain4j"                % "langchain4j"                    % "0.23.0"         % Test,
  "dev.langchain4j"                % "langchain4j-hugging-face"       % "0.23.0"         % Test,
  "com.xebia"                     %% "xef-scala"                      % "0.0.3"          % Test,
  "com.xebia"                      % "xef-pdf"                        % "0.0.3"          % Test,
  "com.xebia"                      % "xef-reasoning-jvm"              % "0.0.3"          % Test,
  "com.xebia"                      % "xef-openai"                     % "0.0.3"          % Test,
  "ch.qos.logback"                 % "logback-classic"                % "1.4.11"         % Test,
  "org.apache.logging.log4j"       % "log4j-core"                     % "2.21.1"         % Test,
  "org.typelevel"                 %% "log4cats-slf4j"                 % "2.6.0"          % Test,
  "org.typelevel"                 %% "toolkit"                        % "latest.release" % Test
)

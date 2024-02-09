scalaVersion := "3.3.1"

lazy val circeVersion       = "0.14.6"
lazy val pureconfigVersion  = "0.17.5"
lazy val redis4CatsVersion  = "1.5.2"
lazy val sttpClient3Version = "3.9.3"
lazy val langchain4jVersion = "0.26.1"
lazy val xefVersion         = "0.0.3"

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
      "com.softwaremill.sttp.client4" %% "core"                           % "4.0.0-M8",
      "com.softwaremill.sttp.client3" %% "core"                           % sttpClient3Version,
      "com.softwaremill.sttp.client3" %% "circe"                          % sttpClient3Version,
      "com.softwaremill.sttp.client3" %% "async-http-client-backend-cats" % sttpClient3Version,
      "de.brendamour"                  % "jpasskit"                       % "0.3.4",
      "com.outr"                      %% "scribe"                         % "3.13.0",
      "com.lihaoyi"                   %% "upickle"                        % "3.1.4",
      "dev.langchain4j"                % "langchain4j"                    % langchain4jVersion,
      "dev.langchain4j"                % "langchain4j-hugging-face"       % langchain4jVersion,
      "com.xebia"                     %% "xef-scala"                      % xefVersion,
      "com.xebia"                      % "xef-pdf"                        % xefVersion,
      "com.xebia"                      % "xef-reasoning-jvm"              % xefVersion,
      "com.xebia"                      % "xef-openai"                     % xefVersion,
      "ch.qos.logback"                 % "logback-classic"                % "1.4.14",
      "org.apache.logging.log4j"       % "log4j-core"                     % "2.22.1",
      "org.typelevel"                 %% "log4cats-slf4j"                 % "2.6.0",
      "co.fs2"                        %% "fs2-core"                       % "3.9.4",
      "org.typelevel"                 %% "munit-cats-effect"              % "2.0.0-M4" % "test"
    )
  )

// Settings
lazy val compilerOptions = Seq(
  "-Xmax-inlines",
  "50",
  "-unchecked",                    // Enable additional warnings where generated code depends on assumptions.
  "-feature",                      // Emit warning and location for usages of features that should be imported explicitly.
  "-language:existentials",        // Existential types (besides wildcard types) can be written and inferred
  "-language:higherKinds",         // Allow higher-kinded types
  "-language:implicitConversions", // Allow definition of implicit functions called views
  "-language:postfixOps",          // Allow postfix operator notation, such as 1 to 10 toList (not recommended)
  "-deprecation"
)

lazy val commonSettings = Seq(
  scalacOptions ++= compilerOptions
)

lazy val scalafixSettings = Seq(semanticdbEnabled := true)

lazy val settings = commonSettings ++ scalafixSettings

//> using scala 3.3.1
//> using toolkit typelevel:latest
//> using packaging.output "dawn-patrol"
//> using nativeMode "release-fast"
//> using resourceDir ./src/resources
//> using mainClass xyz.didx.DawnPatrol
////> using computeVersion git:tag

//> using option -Xmax-inlines 50

//> using dep io.circe::circe-core:0.14.6
//> using dep io.circe::circe-parser:0.14.6
//> using dep io.circe::circe-generic:0.14.6
//> using dep com.github.pureconfig::pureconfig-core:0.17.4
//> using dep com.github.pureconfig::pureconfig-cats-effect:0.17.4
//> using dep dev.profunktor::redis4cats-effects:1.5.2
//> using dep dev.profunktor::redis4cats-log4cats:1.5.2

//> using dep com.softwaremill.sttp.client4::core:4.0.0-M6
//> using dep com.softwaremill.sttp.client3::core:3.9.0
//> using dep com.softwaremill.sttp.client3::circe:3.9.0
//> using dep com.softwaremill.sttp.client3::async-http-client-backend-cats:3.9.0
//> using dep de.brendamour:jpasskit:0.3.3

//> using dep com.outr::scribe:3.12.2
//> using dep com.lihaoyi::upickle:3.1.3
//> using dep dev.langchain4j:langchain4j:0.23.0
//> using dep dev.langchain4j:langchain4j-hugging-face:0.23.0

//> using dep com.xebia::xef-scala:0.0.3
//> using dep com.xebia:xef-pdf:0.0.3 //runtime
//> using dep com.xebia:xef-reasoning-jvm:0.0.3
//> using dep com.xebia:xef-openai:0.0.3 //runtime.pomonly

//> using dep org.slf4j:slf4j-nop:2.0.9

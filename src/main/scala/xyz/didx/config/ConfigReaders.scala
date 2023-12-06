package xyz.didx.config

import cats.data.EitherT
import cats.effect.IO
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import pureconfig.ConfigReader
import pureconfig.ConfigSource
import pureconfig.generic.derivation.default.*
import sttp.client3.ResponseException
import xyz.didx.logging.LogWriter.err
import xyz.didx.logging.LogWriter.info
import xyz.didx.logging.LogWriter.logNonEmptyList

import java.net.URI
import java.net.URL

object ConfigReaders:

  case class AppConf(
    redisUrl: URL,
    redisTimeout: Int,
    ipfsClusterUrl: URL,
    universalResolverUrl: URL,
    universalResolverTimeout: Int,
    ipfsClusterTimeout: Int,
    pollingInterval: Int,
    prismUrl: URL,
    prismToken: String,
    dawnUrl: URL,
    dawnControllerDID: String,
    dawnServiceUrls: Set[URI],
    dawnWelcomeMessage: String,
    protocolsEnabled: List[String]
  ) derives ConfigReader:
    override def toString(): String =
      s"""
         |redisUrl: $redisUrl
         |redisTimeout: $redisTimeout
         |ipfsClusterUrl: $ipfsClusterUrl
         |universalResolverUrl: $universalResolverUrl
         |universalResolverTimeout: $universalResolverTimeout
         |ipfsClusterTimeout: $ipfsClusterTimeout
         |pollingInterval: $pollingInterval
         |prismUrl: $prismUrl
         |prismToken: $prismToken
         |dawnUrl: $dawnUrl
         |dawnControllerDID: $dawnControllerDID
         |dawnServiceUrls: $dawnServiceUrls
         |dawnWelcomeMessage: $dawnWelcomeMessage
         |protocolsEnabled: $protocolsEnabled
         |""".stripMargin

  case class RegistryConf(
    registrarUrl: URL,
    registrarTimeout: Int,
    apiKey: String,
    didMethod: String
  ) derives ConfigReader:
    override def toString(): String =
      s"""
         |registryUrl: $registrarUrl
         |registryTimeout: $registrarTimeout
         |didMethod: $didMethod
         |""".stripMargin

  def getConf(using logger: Logger[IO])         =
    val appConf: AppConf =
      ConfigSource.default.at("app-conf").load[AppConf] match
        case Left(error) =>
          err(s"Error: $error")
          AppConf(
            new URL("http://localhost:8080"),
            0,
            new URL("http://localhost:8080"),
            new URL("http://localhost:8080"),
            0,
            0,
            0,
            new URL("http://localhost:8080"),
            "",
            new URL("http://localhost:8080"),
            "",
            Set(new URI("http://localhost:8080")),
            "WELCOME TO DAWN",
            List()
          )

        case Right(conf) => conf
    appConf
  def getRegistryConf(using logger: Logger[IO]) =
    val registryConf: RegistryConf =
      ConfigSource.default.at("registry-conf").load[RegistryConf] match
        case Left(error) =>
          err(s"Error: $error")
          RegistryConf(new URL("http://localhost:8080"), 0, "", "indy")
        case Right(conf) => conf
    registryConf
  def getSignalConf(using logger: Logger[IO])   =
    val signalConf: SignalConf =
      ConfigSource.default.at("signal-conf").load[SignalConf] match
        case Left(error) =>
          err(s"Error: $error")
          SignalConf(new URL("http://localhost:8080"), 0, "")
        case Right(conf) => conf
    signalConf

  case class SignalConf(
    signalUrl: URL,
    signalTimeout: Int,
    signalPhone: String
  ) derives ConfigReader:

    override def toString(): String =
      s"""
         |signalUrl: $signalUrl
         |signalTimeout: $signalTimeout
         |signalPhone: $signalPhone
         |""".stripMargin

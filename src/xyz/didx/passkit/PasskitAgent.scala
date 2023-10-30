package xyz.didx.passkit

import cats.data.EitherT
import cats.effect.{IO, Resource}
import de.brendamour.jpasskit.{PKBarcode, PKField, PKPass}
import de.brendamour.jpasskit.enums.{PKBarcodeFormat, PKPassType}
import de.brendamour.jpasskit.passes.PKGenericPass
import de.brendamour.jpasskit.signing.{PKFileBasedSigningUtil, PKPassTemplateFolder, PKSigningInformationUtil}
import pureconfig.{ConfigReader, ConfigSource}
import pureconfig.generic.derivation.default.*

import java.awt.Color
import java.io.*
import java.net.URL
import java.nio.charset.Charset
import scala.jdk.CollectionConverters.*
import org.typelevel.log4cats.Logger

case class PasskitConfig(
  keystorePath: String,
  keystorePassword: String,
  appleWWDRCA: String,
  templatePath: String
) derives ConfigReader:
  val keyStoreInputStream: InputStream    =
    getClass.getResourceAsStream(keystorePath)
  val appleWWDRCAInputStream: InputStream =
    getClass.getResourceAsStream(appleWWDRCA)

  override def toString =
    s"""
       |keyStorePath: $keystorePath
       |keyStorePassword: $keystorePassword
       |appleWWDRCA: $appleWWDRCA
       |templatePath: $templatePath
       |""".stripMargin

final case class PasskitAgent(name: String, did: String, dawnURL: URL)(using
  logger: Logger[IO]
):
  def log[T](value: T)(implicit logger: Logger[IO]): IO[Unit] =
    logger.info(s"$value") *> IO.unit
  val passkitConf                                             = getConf()
  def getConf()                                               =
    val passkitConf: PasskitConfig =
      ConfigSource.default.at("passkit-conf").load[PasskitConfig] match
        case Left(error) =>
          log(s"Error: $error")
          PasskitConfig("", "", "", "")
        case Right(conf) => conf
    passkitConf

  def getPass: Either[Exception, PKPass] =
    Right(
      PKPass
        .builder()
        .pass(
          PKGenericPass
            .builder()
            .passType(PKPassType.PKGenericPass)
            .primaryFieldBuilder(
              PKField
                .builder()
                .key("did")
                .label("DID:")
                .value(did)
            )
            .secondaryFieldBuilder(
              PKField
                .builder()
                .key("name")
                .label("AKA:")
                .value(name)
            )
        )
        .barcodeBuilder(
          PKBarcode
            .builder()
            .format(PKBarcodeFormat.PKBarcodeFormatQR)
            .message(dawnURL.toString + "/?did=" + did)
            .messageEncoding(Charset.forName("utf-8"))
        )
        .formatVersion(1)
        .passTypeIdentifier("pass.za.co.didx")
        .serialNumber("000000001")
        .teamIdentifier("UCR5567E6F")
        .organizationName("DIDx")
        .logoText(s"DIDx D@wnPatrol")
        .description(s"$name's D@wnPatrol DID")
        // .backgroundColor(Color.BLACK)
        // .appLaunchURL("https://www.google.com?did=did:example:123")
        // .foregroundColor("rgb(255,255,255 )")

        // ... and more initializations ...
        .build()
    )

  def signPass(): EitherT[IO, Exception, String] =
    for
      pass                <- EitherT(IO.delay(getPass))
      pkSigningInformation = new PKSigningInformationUtil()
                               .loadSigningInformationFromPKCS12AndIntermediateCertificate(
                                 passkitConf.keystorePath,
                                 passkitConf.keystorePassword,
                                 passkitConf.appleWWDRCA
                               )
      pkSigningUtil       <- EitherT.right(IO.delay(new PKFileBasedSigningUtil()))
      passTemplate        <- EitherT.right(
                               IO.delay(new PKPassTemplateFolder(passkitConf.templatePath))
                             )
      passBytes           <- EitherT.right(
                               IO.delay(
                                 pkSigningUtil.createSignedAndZippedPkPassArchive(
                                   pass,
                                   passTemplate,
                                   pkSigningInformation
                                 )
                               )
                             )
      passBase64          <- EitherT.right(PasskitAgent.base64Encode(passBytes))
    yield passBase64

object PasskitAgent:
  def base64Encode(bytes: Array[Byte]): IO[String]          =
    IO.delay(java.util.Base64.getEncoder.encodeToString(bytes))
  def signalAttachment(bytes: Array[Byte]): IO[String]      =
    IO.delay(
      s""""data:application/vnd.apple.pkpass;filename=did.pkpass;base64,${java.util.Base64.getEncoder
          .encodeToString(bytes)}"""
    )
  def inputStream(f: File): Resource[IO, FileInputStream]   =
    Resource.fromAutoCloseable(IO(new FileInputStream(f)))
  def outputStream(f: File): Resource[IO, FileOutputStream] =
    Resource.fromAutoCloseable(IO(new FileOutputStream(f)))

  def inputOutputStreams(
    in: File,
    out: File
  ): Resource[IO, (InputStream, OutputStream)] =
    for {
      inStream  <- inputStream(in)
      outStream <- outputStream(out)
    } yield (inStream, outStream)

  def transmit(
    origin: InputStream,
    destination: OutputStream,
    buffer: Array[Byte],
    acc: Long
  ): IO[Long] =
    for {
      amount <- IO.blocking(origin.read(buffer, 0, buffer.size))
      count  <-
        if (amount > -1)
          IO.blocking(destination.write(buffer, 0, amount)) >> transmit(
            origin,
            destination,
            buffer,
            acc + amount
          )
        else
          IO.pure(
            acc
          ) // End of read stream reached (by java.io.InputStream contract), nothing to write
    } yield count // Returns the actual amount of bytes transmitted // Returns the actual amount of bytes transmitted

  def transfer(origin: InputStream, destination: OutputStream): IO[Long] =
    transmit(origin, destination, new Array[Byte](1024 * 10), 0L)

  def copy(origin: File, destination: File): IO[Long] =
    inputOutputStreams(origin, destination).use { case (in, out) =>
      transfer(in, out)
    }
  def write(f: File, data: Array[Byte]): IO[Long]     =
    Resource.fromAutoCloseable(IO(new FileOutputStream(f))).use { out =>
      IO.blocking(out.write(data)) >> IO.pure(data.length.toLong)
    }

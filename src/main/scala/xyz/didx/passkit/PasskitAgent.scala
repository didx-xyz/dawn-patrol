package xyz.didx.passkit

import cats.data.EitherT
import cats.effect.IO
import cats.effect.Resource
import de.brendamour.jpasskit.PKBarcode
import de.brendamour.jpasskit.PKField
import de.brendamour.jpasskit.PKPass
import de.brendamour.jpasskit.enums.PKBarcodeFormat
import de.brendamour.jpasskit.enums.PKPassType
import de.brendamour.jpasskit.passes.PKGenericPass
import de.brendamour.jpasskit.signing.PKFileBasedSigningUtil
import de.brendamour.jpasskit.signing.PKPassTemplateFolder
import de.brendamour.jpasskit.signing.PKSigningInformationUtil
import org.typelevel.log4cats.Logger
import pureconfig.ConfigReader
import pureconfig.ConfigSource
import pureconfig.generic.derivation.default.*

import java.awt.Color
import java.io.*
import java.net.URL
import java.nio.charset.Charset
import scala.jdk.CollectionConverters.*
import scala.util.Try

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

  def getPass: Either[Error, PKPass] =
    Try(
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
    ).toEither.left.map(e => Error(e.getMessage()))

  def signPass(): IO[Either[Error, String]] =
    (for
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
    yield passBase64).value

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
  ): IO[Either[Error, Long]] =
    IO.blocking(origin.read(buffer, 0, buffer.size)).flatMap { amount =>
      if (amount > -1)
        IO.blocking(destination.write(buffer, 0, amount)) >>
          transmit(origin, destination, buffer, acc + amount)
      else
        IO.pure(Right(acc)) // End of read stream reached, return Right(acc)
    }.handleErrorWith { error =>
      IO.pure(Left(Error(error.getMessage()))) // Return Left(Error) in case of an error
    }

  // Returns the actual amount of bytes transmitted // Returns the actual amount of bytes transmitted

  def transfer(origin: InputStream, destination: OutputStream): IO[Either[Error, Long]] =
    transmit(origin, destination, new Array[Byte](1024 * 10), 0L)
      .handleErrorWith(error => IO.pure(Left(Error(error.getMessage()))))

  def copy(origin: File, destination: File): IO[Either[Error, Long]] =
    inputOutputStreams(origin, destination).use { case (in, out) =>
      transfer(in, out)
    }
  def write(f: File, data: Array[Byte]): IO[Either[Error, Long]]     =
    Resource.fromAutoCloseable(IO(new FileOutputStream(f))).use { out =>
      IO.blocking(out.write(data)) >> IO.pure(Right(data.length.toLong))
    }

package xyz.didx.didcomm

import io.circe.Decoder

sealed trait VerificationMaterial

case class VerificationMaterialJWK(
  crv: String,
  x: String,
  kty: String,
  kid: String
) extends VerificationMaterial:
  override def toString(): String =
    s"""{
      |  "crv": "$crv",
      |  "x": "$x",
      |  "kty": "$kty",
      |  "kid": "$kid"
      """

case class VerificationMaterialMultibase(value: String) extends VerificationMaterial:
  override def toString(): String = s"""{
                                       |  "publicKeyMultibase": "$value"
                                       |}""".stripMargin

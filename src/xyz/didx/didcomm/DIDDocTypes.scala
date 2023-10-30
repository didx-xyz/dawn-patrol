package xyz.didx.didcomm

enum Typ(typ: String) {
  case Encrypted extends Typ("application/didcomm-encrypted+json")
  case Signed    extends Typ("application/didcomm-signed+json")
  case Plaintext extends Typ("application/didcomm-plain+json")
}

object Typ:
  def parse(typ: String): Typ =
    typ match
      case "application/didcomm-encrypted+json" => Typ.Encrypted
      case "application/didcomm-signed+json"    => Typ.Signed
      case "application/didcomm-plain+json"     => Typ.Plaintext
      case _                                    => throw Exception("Unknown type")

enum VerificationMethodType(typ: String):
  case JSON_WEB_KEY_2020             extends VerificationMethodType("JsonWebKey2020")
  case ED25519_VERIFICATION_KEY_2018 extends VerificationMethodType("Ed25519VerificationKey2018")
  case X25519_KEY_AGREEMENT_KEY_2019 extends VerificationMethodType("X25519KeyAgreementKey2019")
  case X25519_KEY_AGREEMENT_KEY_2020 extends VerificationMethodType("X25519KeyAgreementKey2020")
  case ED25519_VERIFICATION_KEY_2020 extends VerificationMethodType("Ed25519VerificationKey2020")
  case ECDSA_SECP_256K1_VERIFICATION_KEY_2019
      extends VerificationMethodType(
        "EcdsaSecp256k1VerificationKey2019"
      ) // - not supported now
  case OTHER extends VerificationMethodType("Other")
  // Ed25519VerificationKey2020

object VerificationMethodType:
  def fromString(typ: String): VerificationMethodType =
    typ match
      case "JsonWebKey2020"                    => VerificationMethodType.JSON_WEB_KEY_2020
      case "Ed25519VerificationKey2018"        =>
        VerificationMethodType.ED25519_VERIFICATION_KEY_2018
      case "X25519KeyAgreementKey2019"         =>
        VerificationMethodType.X25519_KEY_AGREEMENT_KEY_2019
      case "X25519KeyAgreementKey2020"         =>
        VerificationMethodType.X25519_KEY_AGREEMENT_KEY_2020
      case "Ed25519VerificationKey2020"        =>
        VerificationMethodType.ED25519_VERIFICATION_KEY_2020
      case "EcdsaSecp256k1VerificationKey2019" =>
        VerificationMethodType.ECDSA_SECP_256K1_VERIFICATION_KEY_2019
      case _                                   => VerificationMethodType.OTHER

enum VerificationMaterialFormat(val format: String):
  case JWK       extends VerificationMaterialFormat("JWK")
  case BASE58    extends VerificationMaterialFormat("base58")
  case MULTIBASE extends VerificationMaterialFormat("multibase")
  case OTHER     extends VerificationMaterialFormat("other")

object VerificationMaterialFormat:
  def fromString(format: String): VerificationMaterialFormat =
    format match
      case "JWK"       => VerificationMaterialFormat.JWK
      case "base58"    => VerificationMaterialFormat.BASE58
      case "multibase" => VerificationMaterialFormat.MULTIBASE
      case _           => VerificationMaterialFormat.OTHER

enum DIDCommMessageProtocolTypes(val typ: String):
  case Forward
      extends DIDCommMessageProtocolTypes(
        "https://didcomm.org/routing/2.0/forward"
      )

object DIDCommMessageProtocolTypes:
  def parse(typ: String): DIDCommMessageProtocolTypes =
    typ match
      case "https://didcomm.org/routing/2.0/forward" =>
        DIDCommMessageProtocolTypes.Forward
      case _                                         => throw Exception("Unknown type")

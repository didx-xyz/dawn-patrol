package xyz.didx.didcomm

import cats.*
import cats.syntax.all.*
import io.circe.Decoder.Result
import io.circe.*
import xyz.didx.didcomm.DIDDoc
import xyz.didx.didcomm.Service
import xyz.didx.didcomm.ServiceEndpoint
import xyz.didx.didcomm.ServiceEndpointDIDCommService
import xyz.didx.didcomm.ServiceEndpointDIDURL
import xyz.didx.didcomm.ServiceEndpointNodes
import xyz.didx.didcomm.ServiceEndpointURI
import xyz.didx.didcomm.VerificationMethod

import java.net.URI

object DIDCodec:
  given encodeDIDDoc: Encoder[DIDDoc] =
    new Encoder[DIDDoc]:
      final def apply(a: DIDDoc): Json =
        Json.obj(
          (
            "didDocument",
            Json.obj(
              ("id", Json.fromString(a.did)),
              ("controller", Json.fromString(a.controller.getOrElse(""))),
              (
                "alsoKnownAs",
                Json.fromValues(
                  a.alsoKnownAs.getOrElse(Set.empty).map(Json.fromString)
                )
              ),
              (
                "verificationMethod",
                Json.fromValues(
                  a.verificationMethods
                    .getOrElse(Set.empty)
                    .map(encodeVerificationMethod.apply)
                )
              ),
              (
                "keyAgreement",
                Json.fromValues(
                  a.keyAgreements
                    .getOrElse(Set.empty)
                    .map(encodeKeyAgreement.apply)
                )
              ),
              (
                "authentication",
                Json.fromValues(
                  a.authentications
                    .getOrElse(Set.empty)
                    .map(encodeAuthentication.apply)
                )
              ),
              (
                "assertionMethod",
                Json.fromValues(
                  a.assertionMethods
                    .getOrElse(Set.empty)
                    .map(encodeAssertion.apply)
                )
              ),
              (
                "capabilityInvocation",
                Json.fromValues(
                  a.capabilityInvocations
                    .getOrElse(Set.empty)
                    .map(encodeCapabilityInvocation.apply)
                )
              ),
              (
                "capabilityDelegations",
                Json.fromValues(
                  a.capabilityDelegations
                    .getOrElse(Set.empty)
                    .map(encodeCapabilityDelegation.apply)
                )
              ),
              (
                "service",
                Json.fromValues(
                  a.services.getOrElse(Set.empty).map(encodeService.apply)
                )
              )
            )
          )
        )

  given encodeVerificationMethod: Encoder[VerificationMethod] =
    new Encoder[VerificationMethod]:
      final def apply(a: VerificationMethod): Json =
        Json.obj(
          ("id", Json.fromString(a.id)),
          ("type", Json.fromString(a.`type`.toString())),
          ("controller", Json.fromString(a.controller)),
          ("public", encodeVerificationMaterial.apply(a.verificationMaterial))
        )

  given encodeVerificationMaterial: Encoder[VerificationMaterial] =
    new Encoder[VerificationMaterial]:
      final def apply(a: VerificationMaterial): Json =
        a match
          case VerificationMaterialJWK(crv, x, kty, kid) =>
            Json.obj(
              ("crv", Json.fromString(crv)),
              ("x", Json.fromString(x)),
              ("kty", Json.fromString(kty)),
              ("kid", Json.fromString(kid))
            )
          case VerificationMaterialMultibase(value)      =>
            Json.fromString(value)

  given encodeKeyAgreement: Encoder[KeyAgreement] =
    new Encoder[KeyAgreement]:
      final def apply(a: KeyAgreement): Json =
        a match {
          case KeyAgreementInstance(crv, x, kty, kid) =>
            Json.obj(
              ("crv", Json.fromString(crv)),
              ("x", Json.fromString(x)),
              ("kty", Json.fromString(kty)),
              ("kid", Json.fromString(kid))
            )
          case KeyAgreementReference(value)           =>
            Json.fromString(value)
        }

  given encoderKeyAgreementInstance: Encoder[KeyAgreementInstance] =
    new Encoder[KeyAgreementInstance]:
      final def apply(a: KeyAgreementInstance): Json =
        Json.obj(
          ("id", Json.fromString(a.id)),
          ("type", Json.fromString(a.`type`)),
          ("controller", Json.fromString(a.controller)),
          ("publicKeyMultibase", Json.fromString(a.publicKeyMultibase))
        )

  given encodeAuthentication: Encoder[Authentication] =
    new Encoder[Authentication]:
      final def apply(a: Authentication): Json =
        a match {
          case AuthenticationInstance(crv, x, kty, kid) =>
            Json.obj(
              ("crv", Json.fromString(crv)),
              ("x", Json.fromString(x)),
              ("kty", Json.fromString(kty)),
              ("kid", Json.fromString(kid))
            )
          case AuthenticationReference(value)           =>
            Json.fromString(value)
        }

  given encodeAssertion: Encoder[Assertion] =
    new Encoder[Assertion]:
      final def apply(a: Assertion): Json =
        a match {
          case AssertionInstance(crv, x, kty, kid) =>
            Json.obj(
              ("crv", Json.fromString(crv)),
              ("x", Json.fromString(x)),
              ("kty", Json.fromString(kty)),
              ("kid", Json.fromString(kid))
            )
          case AssertionReference(value)           =>
            Json.fromString(value)
        }

  given encodeCapabilityInvocation: Encoder[CapabilityInvocation] =
    new Encoder[CapabilityInvocation]:
      final def apply(a: CapabilityInvocation): Json =
        a match {
          case CapabilityInvocationInstance(crv, x, kty, kid) =>
            Json.obj(
              ("crv", Json.fromString(crv)),
              ("x", Json.fromString(x)),
              ("kty", Json.fromString(kty)),
              ("kid", Json.fromString(kid))
            )
          case CapabilityInvocationReference(value)           =>
            Json.fromString(value)
        }

  given encodeCapabilityDelegation: Encoder[CapabilityDelegation] =
    new Encoder[CapabilityDelegation]:
      final def apply(a: CapabilityDelegation): Json =
        a match {
          case CapabilityDelegationInstance(crv, x, kty, kid) =>
            Json.obj(
              ("crv", Json.fromString(crv)),
              ("x", Json.fromString(x)),
              ("kty", Json.fromString(kty)),
              ("kid", Json.fromString(kid))
            )
          case CapabilityDelegationReference(value)           =>
            Json.fromString(value)
        }

  given encodeURI: Encoder[URI] =
    new Encoder[URI]:
      final def apply(u: URI): Json = Json.fromString(u.toString)

  given encodeService: Encoder[Service] =
    new Encoder[Service]:
      final def apply(a: Service): Json =
        Json.obj(
          ("id", Json.fromString(a.id.toString)),
          (
            "type",
            Json.fromValues(a.`type`.map(_.toString).map(Json.fromString))
          ),
          (
            "serviceEndpoint",
            Json.fromValues(
              a.serviceEndpoint.map(encodeDIDCommServiceEndpoint.apply)
            )
          )
        )

  given encodeDIDCommServiceEndpoint: Encoder[ServiceEndpoint] =
    new Encoder[ServiceEndpoint]:
      final def apply(a: ServiceEndpoint): Json =
        a match {
          case ServiceEndpointURI(value)                               => Json.fromString(value.toString)
          case ServiceEndpointDIDURL(did, fragment)                    =>
            Json.obj(
              "did"      -> Json.fromString(did),
              "fragment" -> Json.fromString(fragment)
            )
          case ServiceEndpointDIDCommService(uri, accept, routingKeys) =>
            Json.obj(
              "uri"         -> Json.fromString(uri.toString),
              "accept"      -> Json.fromValues(
                accept.map(_.toString).map(Json.fromString)
              ),
              "routingKeys" -> Json.fromValues(
                routingKeys.map(_.toString).map(Json.fromString)
              )
            )
          case ServiceEndpointNodes(nodes)                             =>
            Json.obj(
              "nodes" -> Json.fromValues(
                nodes.map(n => n.toString).map(Json.fromString)
              )
            )
        }

  given decodeDIDDoc: Decoder[DIDDoc] =
    new Decoder[DIDDoc]:
      final def apply(cur: HCursor): Decoder.Result[DIDDoc] =
        val c = cur.downField("didDocument").success match
          case Some(c) => c
          case None    => cur

        for {
          did                                         <- c.downField("id").as[Option[String]]
          controller                                  <- c
                                                           .downField("controller")
                                                           .as[Option[String]]
          alsoKnownAs                                 <- c
                                                           .downField("alsoKnownAs")
                                                           .as[Option[Set[String]]]
          verificationMethod                          <- c
                                                           .downField("verificationMethod")
                                                           .as[Option[Set[VerificationMethod]]]
          keyAgreement: Option[Set[KeyAgreement]]     <- c
                                                           .downField("keyAgreement")
                                                           .as[Option[Set[KeyAgreement]]]
          authentication: Option[Set[Authentication]] <- c
                                                           .downField("authentication")
                                                           .as[Option[Set[Authentication]]]
          assertionMethod                             <- c
                                                           .downField("assertionMethod")
                                                           .as[Option[Set[Assertion]]]
          capabilityInvocation                        <- c
                                                           .downField("capabilityInvocation")
                                                           .as[Option[Set[CapabilityInvocation]]]
          capabilityDelegations                       <- c
                                                           // .downField("didDocument")
                                                           .downField("capabilityDelegations")
                                                           .as[Option[Set[CapabilityDelegation]]]
          didCommServices                             <- c
                                                           .downField("service")
                                                           .as[Option[Set[Service]]]
        } yield DIDDoc(
          did.getOrElse(""),
          controller,
          alsoKnownAs,
          verificationMethod,
          keyAgreement,
          authentication,
          assertionMethod,
          capabilityInvocation,
          capabilityDelegations,
          didCommServices
        )

  given decodeVerificationMethod: Decoder[VerificationMethod] =
    new Decoder[VerificationMethod]:
      final def apply(c: HCursor): Decoder.Result[VerificationMethod] =
        for {
          id                   <- c.downField("id").as[String]
          `type`               <- c.downField("type").as[String]
          controller           <- c.downField("controller").as[String]
          verificationMaterial <- c
                                    .downField("publicKeyJwk")
                                    .as[VerificationMaterialJWK]
                                    .orElse(
                                      c.downField("publicKeyMultibase")
                                        .as[VerificationMaterialMultibase]
                                        .orElse(
                                          c.downField("publicKeyBase58")
                                            .as[VerificationMaterialMultibase]
                                        )
                                    )

        } yield VerificationMethod(
          id,
          VerificationMethodType.fromString(`type`),
          verificationMaterial,
          controller
        )

  given decodeVerificationMaterialJWK: Decoder[VerificationMaterialJWK] =
    Decoder.forProduct4("crv", "x", "kty", "kid")(VerificationMaterialJWK.apply)

  given decodeVerificationMaterialMultibase: Decoder[VerificationMaterialMultibase] =
    new Decoder[VerificationMaterialMultibase]:
      final def apply(
        c: HCursor
      ): Decoder.Result[VerificationMaterialMultibase] =
        for {
          value <- c.value.as[String]
        } yield VerificationMaterialMultibase(value)

  given decodeVerificationMaterial: Decoder[VerificationMaterial] =
    decodeVerificationMaterialJWK
      .widen[VerificationMaterial] or decodeVerificationMaterialMultibase
      .widen[VerificationMaterial]

  given decodeAuthenticationReference: Decoder[AuthenticationReference] =
    new Decoder[AuthenticationReference]:
      final def apply(
        c: HCursor
      ): Decoder.Result[AuthenticationReference] =
        for {
          value <- c.value.as[String]
        } yield AuthenticationReference(value)

  given decodeAuthenticationInstance: Decoder[AuthenticationInstance] =
    Decoder.forProduct4("id", "type", "controller", "publicKeyMultibase")(
      AuthenticationInstance.apply
    )

  given decodeAuthenticationRelationship: Decoder[Authentication] =
    decodeAuthenticationReference
      .widen[Authentication] or decodeAuthenticationInstance
      .widen[Authentication]

  given decodeAssertionInstance: Decoder[AssertionInstance] =
    new Decoder[AssertionInstance]:
      final def apply(c: HCursor): Decoder.Result[AssertionInstance] =
        for {
          id                 <- c.downField("id").as[String]
          `type`             <- c.downField("type").as[String]
          controller         <- c.downField("controller").as[String]
          publicKeyMultibase <- c
                                  .downField("publicKeyMultibase")
                                  .as[String]
                                  .orElse(
                                    c.downField("publicKeyBase58")
                                      .as[String]
                                  )
        } yield AssertionInstance(
          id,
          `type`,
          controller,
          publicKeyMultibase
        )

  given decodeAssertionReference: Decoder[AssertionReference] =
    new Decoder[AssertionReference]:
      final def apply(c: HCursor): Decoder.Result[AssertionReference] =
        for {
          value <- c.value.as[String]
        } yield AssertionReference(value)

  given decodeAssertion: Decoder[Assertion] =
    decodeAssertionInstance.widen[Assertion] or decodeAssertionReference
      .widen[Assertion]

  given decodeKeyAgreementMethod: Decoder[KeyAgreementInstance] =
    new Decoder[KeyAgreementInstance]:
      final def apply(c: HCursor): Decoder.Result[KeyAgreementInstance] =
        for {
          id                 <- c.downField("id").as[String]
          `type`             <- c.downField("type").as[String]
          controller         <- c.downField("controller").as[String]
          publicKeyMultibase <- c
                                  .downField("publicKeyMultibase")
                                  .as[String]
                                  .orElse(
                                    c.downField("publicKeyBase58")
                                      .as[String]
                                  )
        } yield KeyAgreementInstance(
          id,
          `type`,
          controller,
          publicKeyMultibase
        )

  given decodeKeyAgreementReference: Decoder[KeyAgreementReference] =
    new Decoder[KeyAgreementReference]:
      final def apply(c: HCursor): Decoder.Result[KeyAgreementReference] =
        for {
          value <- c.value.as[String]
        } yield KeyAgreementReference(value)

  given decodeKeyAgreement: Decoder[KeyAgreement] =
    decodeKeyAgreementReference.widen[KeyAgreement] or decodeKeyAgreementMethod
      .widen[KeyAgreement]

  given decodeCapabilityInvocationInstance: Decoder[CapabilityInvocationInstance] =
    new Decoder[CapabilityInvocationInstance]:
      final def apply(
        c: HCursor
      ): Decoder.Result[CapabilityInvocationInstance] =
        for {
          id                 <- c.downField("id").as[String]
          `type`             <- c.downField("type").as[String]
          controller         <- c.downField("controller").as[String]
          publicKeyMultibase <- c
                                  .downField("publicKeyMultibase")
                                  .as[String]
                                  .orElse(
                                    c.downField("publicKeyBase58")
                                      .as[String]
                                  )
        } yield CapabilityInvocationInstance(
          id,
          `type`,
          controller,
          publicKeyMultibase
        )

  given decodeCapabilityInvocationReference: Decoder[CapabilityInvocationReference] =
    new Decoder[CapabilityInvocationReference]:
      final def apply(
        c: HCursor
      ): Decoder.Result[CapabilityInvocationReference] =
        for {
          value <- c.value.as[String]
        } yield CapabilityInvocationReference(value)

  given decodeCapabilityInvocation: Decoder[CapabilityInvocation] =
    decodeCapabilityInvocationInstance
      .widen[CapabilityInvocation] or decodeCapabilityInvocationReference
      .widen[CapabilityInvocation]

  given decodeCapabilityDelegationInstance: Decoder[CapabilityDelegationInstance] =
    new Decoder[CapabilityDelegationInstance]:
      final def apply(
        c: HCursor
      ): Decoder.Result[CapabilityDelegationInstance] =
        for {
          id                 <- c.downField("id").as[String]
          `type`             <- c.downField("type").as[String]
          controller         <- c.downField("controller").as[String]
          publicKeyMultibase <- c
                                  .downField("publicKeyMultibase")
                                  .as[String]
                                  .orElse(
                                    c.downField("publicKeyBase58")
                                      .as[String]
                                  )
        } yield CapabilityDelegationInstance(
          id,
          `type`,
          controller,
          publicKeyMultibase
        )

  given decodeCapabilityDelegationReference: Decoder[CapabilityDelegationReference] =
    new Decoder[CapabilityDelegationReference]:
      final def apply(
        c: HCursor
      ): Decoder.Result[CapabilityDelegationReference] =
        for {
          value <- c.value.as[String]
        } yield CapabilityDelegationReference(value)

  given decodeCapabilityDelegation: Decoder[CapabilityDelegation] =
    decodeCapabilityDelegationInstance
      .widen[CapabilityDelegation] or decodeCapabilityDelegationReference
      .widen[CapabilityDelegation]

  given decodeServiceEndpointURI: Decoder[ServiceEndpointURI] =
    new Decoder[ServiceEndpointURI]:
      final def apply(c: HCursor): Decoder.Result[ServiceEndpointURI] =
        for {
          value <- c.value.as[String]
        } yield ServiceEndpointURI(new URI(value))

  given decodeServiceEndpointDIDURL: Decoder[ServiceEndpointDIDURL] =
    new Decoder[ServiceEndpointDIDURL]:
      final def apply(c: HCursor): Decoder.Result[ServiceEndpointDIDURL] =
        for {
          did      <- c.downField("did").as[String]
          fragment <- c.value.as[String]
        } yield ServiceEndpointDIDURL(did, fragment)

  given decodeServiceEndpointDIDCommService: Decoder[ServiceEndpointDIDCommService] =
    new Decoder[ServiceEndpointDIDCommService]:
      final def apply(
        c: HCursor
      ): Decoder.Result[ServiceEndpointDIDCommService] =
        for {
          uri         <- c.downField("uri").as[URI]
          accept      <- c.downField("accept").as[Option[Set[String]]]
          routingKeys <- c.downField("routingKeys").as[Option[Set[String]]]
        } yield ServiceEndpointDIDCommService(uri, accept, routingKeys)

  given decodeServiceEndpointNodes: Decoder[ServiceEndpointNodes] =
    new Decoder[ServiceEndpointNodes]:
      final def apply(c: HCursor): Decoder.Result[ServiceEndpointNodes] =
        for {
          nodes <- c.downField("nodes").as[Set[URI]]
        } yield ServiceEndpointNodes(nodes)

  given decodeServiceEndpoint: Decoder[ServiceEndpoint] =
    decodeServiceEndpointURI
      .widen[ServiceEndpoint] or decodeServiceEndpointDIDURL
      .widen[ServiceEndpoint] or decodeServiceEndpointDIDCommService
      .widen[ServiceEndpoint] or decodeServiceEndpointNodes
      .widen[ServiceEndpoint]

  given decodeService: Decoder[Service] =
    new Decoder[Service]:
      final def apply(c: HCursor): Result[Service] =
        for {
          id              <- c.downField("id").as[String].orElse(Right("did:sov:123"))
          `type`          <- c.downField("type").focus match
                               case Some(value) if value.isString  =>
                                 Right(Set[String](value.asString.getOrElse("")))
                               case Some(values) if values.isArray =>
                                 Right(
                                   values.asArray
                                     .map(_.map(_.asString.getOrElse("")).toSet)
                                     .getOrElse(Set.empty[String])
                                 )
                               case _                              => Right(Set.empty[String])
          serviceEndpoint <- c.downField("serviceEndpoint").focus match
                               case Some(value) if value.isString  =>
                                 Right(
                                   Set(ServiceEndpointURI(new URI(value.asString.getOrElse(""))))
                                 )
                               case Some(values) if values.isArray =>
                                 values.as[Set[ServiceEndpoint]]
                               case Some(value) if value.isObject  =>
                                 value.as[ServiceEndpoint].map(Set(_))
                               case _                              => Right(Set(ServiceEndpointURI(new URI(""))))
        } yield Service(
          new URI(id),
          `type`,
          serviceEndpoint
        )

package xyz.didx.didcomm

//import org.didcommx.didcomm.common.VerificationMaterial
//import org.didcommx.didcomm.common.VerificationMethodType

import io.circe._
import io.circe.syntax._
import xyz.didx.didcomm.DIDCodec
import xyz.didx.didcomm.VerificationMethodType

import java.net.URI
import scala.util.matching.Regex

//import foundation.identity.did.VerificationMethod

/**
 * DID DOC (https://www.w3.org/TR/did-core/#dfn-did-documents)
 * @property
 *   did a DID for the given DID Doc
 * @property
 *   keyAgreements Key IDs (DID URLs) of all verification methods from the
 *   'keyAgreement' verification relationship in this DID DOC. See
 *   https://www.w3.org/TR/did-core/#verification-methods.
 * @property
 *   authentications Key IDs (DID URLs) of all verification methods from the
 *   'authentication' verification relationship in this DID DOC. See
 *   https://www.w3.org/TR/did-core/#authentication.
 * @property
 *   verificationMethods Returns all local verification methods including
 *   embedded to key agreement and authentication sections. See
 *   https://www.w3.org/TR/did-core/#verification-methods.
 * @property
 *   didCommServices All services of 'DIDCommMessaging' type in this DID DOC.
 *   Empty list is returned if there are no services of 'DIDCommMessaging' type.
 *   See https://www.w3.org/TR/did-core/#services and
 *   https://identity.foundation/didcomm-messaging/spec/#did-document-service-endpoint.
 */

object DIDTypes {
  opaque type DIDUrl           = String
  opaque type Method           = String
  opaque type MethodSpecificId = String

  private val methodNamePattern: Regex                = """[a-z0-9]+""".r
  private val methodSpecificIdPattern: Regex          = """(:[a-z0-9]+)*[a-z0-9]+""".r
  private val supportedMethods: List[String]          =
    List("example", "ion", "key", "indy", "web", "prism")
  def createMethodName(value: String): Option[Method] =
    methodNamePattern.findFirstIn(value) match {
      case Some(m) if supportedMethods.contains(m) => Some(m)
      case _                                       => None
    }

  def createMethodSpecificId(value: String): Option[MethodSpecificId] =
    methodSpecificIdPattern.findFirstIn(value).map(_ => value)

  def extractMethodName(value: String): Option[Method]                 =
    value.split(":").tail.headOption.flatMap(createMethodName)
  def extractMethodSpecificId(value: String): Option[MethodSpecificId] =
    value.split(":").tail.tail.headOption.flatMap(createMethodSpecificId)
  /*  def fromDIDUrl(didUrl: DIDUrl): Option[DID] =
    for {
      methodName <- extractMethodName(didUrl)
      methodSpecificId <- extractMethodSpecificId(didUrl)
    } yield DID(methodName, methodSpecificId) */
  def fromDIDUrl(value: DIDUrl): Option[DID]                           =
    value.split(":").toList match {
      case "did" :: methodName :: methodSpecificId :: Nil =>
        for {
          methodName       <- createMethodName(methodName)
          methodSpecificId <- createMethodSpecificId(methodSpecificId)
        } yield DID(methodName, methodSpecificId)
      case _                                              => None
    }
}

import DIDTypes._

case class DID(methodName: Method, methodSpecificId: MethodSpecificId) {
  override def toString: String = s"did:$methodName:$methodSpecificId "
  def toDIDUrl: DIDUrl          = toString.asInstanceOf[DIDUrl]

}

case class DIDDoc(
  did: String = "",
  controller: Option[String] = None,
  alsoKnownAs: Option[Set[String]] = None,
  verificationMethods: Option[Set[VerificationMethod]] = None,
  keyAgreements: Option[Set[KeyAgreement]] = None,
  authentications: Option[Set[Authentication]] = None,
  assertionMethods: Option[Set[Assertion]] = None,
  capabilityInvocations: Option[Set[CapabilityInvocation]] = None,
  capabilityDelegations: Option[Set[CapabilityDelegation]] = None,
  services: Option[Set[Service]] = None
):

  /*  def findVerificationMethod(
      id: String
  ): Either[DIDUrlNotFoundException, VerificationMethod] =
    verificationMethods match
      case Some(v) =>
        v.find(_.id == id) match
          case Some(v: VerificationMethod) => Right(v)
          case _                           => Left(DIDUrlNotFoundException(id, did))
      case _ => Left(DIDUrlNotFoundException(id, did))

  def findDIDCommService(id: String): Either[DIDDocException, Service] =
    services match
      case Some(v: Service) =>
        v.find(_.id.toString() == id) match
          case Some(v: Service) => Right(v)
          case _                => Left(DIDDocException("DIDComm service not found"))
      case _ => Left(DIDDocException("DIDComm service not found"))
   */
  override def toString: String =
    s"""DIDDoc(
       |  did=$did,
       |  controller=$controller,
       |  alsoKnownAs=$alsoKnownAs,
       |  verificationMethods=$verificationMethods,
       |  keyAgreements=$keyAgreements,
       |  authentications=$authentications,
       |  assertionMethods=$assertionMethods,
       |  capabilityInvocations=$capabilityInvocations,
       |  capabilityDelegations=$capabilityDelegations,
       |  services=$services
       |)""".stripMargin

object DIDDoc:
  import DIDCodec.*
  import DIDCodec.encodeDIDDoc
  import DIDCodec.decodeDIDDoc

  // def apply(did: String, controller: Option[String], alsoKnownAs: Option[Set[String]], verificationMethods: Option[Set[VerificationMethod]], keyAgreements: Option[Set[KeyAgreement]], authentications: Option[Set[Authentication]], assertionMethods: Option[Set[Assertion]], capabilityInvocations: Option[Set[CapabilityInvocation]], capabilityDelegations: Option[Set[CapabilityDelegation]], services: Option[Set[Service]]): DIDDoc = new DIDDoc(did, controller, alsoKnownAs, verificationMethods, keyAgreements, authentications, assertionMethods, capabilityInvocations, capabilityDelegations, services)

  def createDIDKeyDocument(
    did: String,
    controller: String,
    verificationMethod: VerificationMethod,
    service: Service
  ): DIDDoc =
    DIDDoc(
      did,
      Some(controller),
      None,
      Some(Set(verificationMethod)),
      None,
      None,
      None,
      None,
      None,
      Some(Set(service))
    )
  def addContext(didDoc: DIDDoc, contexts: List[String]): DIDDoc =
    val didDocJson            = didDoc.asJson
    val didDocJsonWithContext =
      didDocJson.mapObject(_.add("@context", contexts.asJson))
    didDocJsonWithContext.as[DIDDoc].getOrElse(didDoc)

/**
 * DID DOC Verification method. It can be used in such verification
 * relationships as Authentication, KeyAgreement, etc. See
 * https://www.w3.org/TR/did-core/#verification-methods.
 */
case class VerificationMethod(
  id: String,
  `type`: VerificationMethodType,
  verificationMaterial: VerificationMaterial,
  controller: String
):
  override def toString: String =
    s"""VerificationMethod(
       |  id=$id,
       |  type= ${`type`.toString()},
       |  verificationMaterial=$verificationMaterial,
       |  controller=$controller
       |)""".stripMargin

/**
 * DID DOC Service of 'DIDCommMessaging' type. see
 * https://www.w3.org/TR/did-core/#services and
 * https://identity.foundation/didcomm-messaging/spec/#did-document-service-endpoint.
 *
 * @property
 *   id Service's 'id' field
 * @property
 *   serviceEndpoint A service endpoint. It can be either a URI to be used for
 *   transport or a mediator's DID in case of alternative endpoints.
 * @property
 *   routingKeys A possibly empty ordered array of strings referencing keys to
 *   be used when preparing the message for transmission.
 * @property
 *   accept A possibly empty ordered array of strings representing accepted
 *   didcomm specification versions.
 */

case class Service(
  id: URI,
  `type`: Set[String],
  serviceEndpoint: Set[ServiceEndpoint]
):
  override def toString: String =
    s"""Service(
       |  id=$id,
       |  type= ${`type`},
       |  serviceEndpoint=$serviceEndpoint
       |)""".stripMargin

sealed trait ServiceEndpoint

case class ServiceEndpointURI(uri: URI) extends ServiceEndpoint:
  override def toString: String =
    s"""ServiceEndpointURI(
       |  uri=$uri
       |)""".stripMargin

case class ServiceEndpointNodes(nodes: Set[URI]) extends ServiceEndpoint:
  override def toString: String =
    s"""ServiceEndpointNodes(
       |  nodes=$nodes
       |)""".stripMargin

case class ServiceEndpointDIDURL(did: String, fragment: String) extends ServiceEndpoint:
  override def toString: String =
    s"""ServiceEndpointDIDURL(
       |  did=$did,
       |  fragment=$fragment
       |)""".stripMargin

case class ServiceEndpointDIDCommService(
  uri: URI,
  accept: Option[Set[String]],
  routingKeys: Option[Set[String]]
) extends ServiceEndpoint:
  override def toString: String =
    s"""ServiceEndpointDIDCommService(
       |  uri=$uri,
       |  accept=$accept,
       |  routingKeys=$routingKeys
       |)""".stripMargin

package xyz.didx.didcomm

sealed trait VerificationRelationship

sealed trait Authentication

sealed trait Assertion

sealed trait KeyAgreement

sealed trait CapabilityDelegation

sealed trait CapabilityInvocation

sealed trait VerificationReference(ref: String) extends VerificationRelationship

sealed trait VerificationInstance(
  id: String,
  `type`: String,
  controller: String,
  publicKeyMultibase: String
) extends VerificationRelationship:
  override def toString: String =
    s"""VerificationInstance(
       |  id=$id,
       |  type= ${`type`},
       |  controller=$controller,
       |  publicKeyMultibase=$publicKeyMultibase
       |)""".stripMargin

case class AuthenticationInstance(
  id: String,
  `type`: String,
  controller: String,
  publicKeyMultibase: String
) extends VerificationInstance(id, `type`, controller, publicKeyMultibase)
    with Authentication
case class AuthenticationReference(ref: String)
    extends VerificationReference(ref)
    with Authentication
case class AssertionInstance(
  id: String,
  `type`: String,
  controller: String,
  publicKeyMultibase: String
) extends VerificationInstance(id, `type`, controller, publicKeyMultibase)
    with Assertion

case class AssertionReference(ref: String) extends VerificationReference(ref) with Assertion

case class CapabilityDelegationInstance(
  id: String,
  `type`: String,
  controller: String,
  publicKeyMultibase: String
) extends VerificationInstance(id, `type`, controller, publicKeyMultibase)
    with CapabilityDelegation

case class CapabilityDelegationReference(ref: String)
    extends VerificationReference(ref)
    with CapabilityDelegation

case class CapabilityInvocationInstance(
  id: String,
  `type`: String,
  controller: String,
  publicKeyMultibase: String
) extends VerificationInstance(id, `type`, controller, publicKeyMultibase)
    with CapabilityInvocation

case class CapabilityInvocationReference(ref: String)
    extends VerificationReference(ref)
    with CapabilityInvocation

case class KeyAgreementInstance(
  id: String,
  `type`: String,
  controller: String,
  publicKeyMultibase: String
) extends VerificationInstance(id, `type`, controller, publicKeyMultibase)
    with KeyAgreement

case class KeyAgreementReference(ref: String) extends VerificationReference(ref) with KeyAgreement

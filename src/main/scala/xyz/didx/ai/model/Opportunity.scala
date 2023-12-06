package xyz.didx.ai.model

import io.circe.Decoder
import io.circe.DecodingFailure
import io.circe.HCursor

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

case class Opportunity(
  skills: List[String],
  countries: List[String],
  language: List[String],
  unverifiedCredentials: Int,
  approvedCredentials: Int,
  rejectedCredentials: Int,
  totalZLTORewarded: Double,
  skillsLearned: Int,
  opportunityURL: String,
  organisationId: String,
  organisationName: String,
  organisationLogoURL: Option[String],
  organisationURL: String,
  organisationPrimaryContactName: String,
  organisationPrimaryContactEmail: String,
  organisationPrimaryContactPhone: Option[String],
  id: String,
  title: String,
  description: String,
  instructions: Option[String],
  url: Option[String],
  createdAt: LocalDateTime,
  zltoReward: Option[Double],
  createdByAdmin: Boolean,
  difficulty: String,
  timeValue: Option[Int],
  timePeriod: String,
  startTime: LocalDateTime,
  endTime: LocalDateTime,
  published: Boolean,
  `type`: String,
  noEndDate: Boolean,
  participantCount: Option[Int]
)

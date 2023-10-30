package xyz.didx.connection

import dev.profunktor.redis4cats.Redis
import cats.effect.kernel.Resource
import cats.effect.IO
import dev.profunktor.redis4cats.RedisCommands
import io.circe.syntax.*
import cats.effect._
import cats.implicits._
import dev.profunktor.redis4cats.Redis
import dev.profunktor.redis4cats.RedisCommands

import dev.profunktor.redis4cats.effect.Log.Stdout._
import xyz.didx.didcomm.DIDTypes.*

import scala.concurrent.duration.FiniteDuration

object RedisStorage:
  def create(redisUrl: String): Resource[IO, RedisStorage] =
    Redis[IO].utf8(redisUrl).map(cmd => new RedisStorage(cmd))

class RedisStorage(redis: RedisCommands[IO, String, String]):
  def writeToRedis(did: DIDUrl, aspect: String, words: List[String]): IO[Unit] =
    redis.set(s"$did:$aspect", words.asJson.noSpaces)
  def readFromRedis(did: DIDUrl, aspect: String): IO[Option[String]]           =
    redis.get(s"$did:$aspect")

  def readAllFromRedis(did: DIDUrl): IO[List[(String, String)]]    =
    redis.keys(s"$did:*").flatMap { keys =>
      keys.traverse { key =>
        redis.get(key).map { value =>
          (key, value.getOrElse(""))
        }
      }
    }
  def storePhoneNumber(did: DIDUrl, phoneNumber: String): IO[Unit] =
    for {
      _ <- redis.set(s"$did:phoneNumber", phoneNumber)
      _ <- redis.set(s"$phoneNumber:did", did.toString())
    } yield ()

  def getPhoneNumber(did: DIDUrl): IO[Option[String]] =
    redis.get(s"$did:phoneNumber")

  def getDidByPhoneNumber(phoneNumber: String): IO[Option[DIDUrl]] =
    redis.get(s"$phoneNumber:did").asInstanceOf[IO[Option[DIDUrl]]]

  def storeEmail(did: DIDUrl, email: String): IO[Unit] =
    for {
      _ <- redis.set(s"$did:email", email)
      _ <- redis.set(s"$email:did", did.toString())
    } yield ()

  def getEmail(did: DIDUrl): IO[Option[String]] =
    redis.get(s"$did:email")

  def getDidByEmail(email: String): IO[Option[DIDUrl]] =
    redis.get(s"$email:did").asInstanceOf[IO[Option[DIDUrl]]]

  /*  def getOrCreateAgent(clientId: DIDUrl, ttl: FiniteDuration): IO[ConversationAgent] =
      redis.get(clientId.toString()).flatMap {
        case Some(_) =>
          // Reset the TTL since there was an interaction
          redis.expire(clientId.toString(), ttl) *> IO.pure(ConversationAgent(clientId))
        case None =>
          // Create a new agent and set with a TTL
          redis.setEx(clientId.toString, ConversationAgent(clientId).asJson.noSpaces, ttl) *> IO.pure(ConversationAgent(clientId))
      }   */

package oen.billstracker.endpoints

import cats.effect.Effect
import org.http4s.dsl.Http4sDsl

import cats.implicits._
import cats.effect._
import org.http4s._
import org.http4s.server._
import org.http4s.circe._
import oen.billstracker.shared.Dto.User
import io.circe.syntax._
import io.circe.generic.auto._
import oen.billstracker.model.StorageData._
import io.scalaland.chimney.dsl._
import org.log4s._

class UserEndpoints[F[_] : Effect](authMiddleware: AuthMiddleware[F, DbUser]) extends Http4sDsl[F] {
  private[this] implicit val logger: Logger = getLogger(getClass)
  private[this] implicit val userDecoder = jsonOf[F, User]

  val authedEndpoints: AuthedService[DbUser, F] = AuthedService {
    case GET -> Root / "user" as user => Ok(user.into[User].transform.asJson)

    case authReq @ POST -> Root / "user" as user => Ok(for { // TODO remove this
      resp <- Effect[F].delay("ok?")
      reqMsg <- authReq.req.as[User]
      _ <- Effect[F].delay {
        logger.debug(reqMsg.toString)
        logger.debug(reqMsg.into[DbUser].transform.toString)
      }
    } yield resp)
  }
  val endpoints: HttpRoutes[F] = authMiddleware(authedEndpoints)
}

object UserEndpoints {
  def apply[F[_] : Effect](authMiddleware: AuthMiddleware[F, DbUser]): UserEndpoints[F] = new UserEndpoints[F](authMiddleware)
}

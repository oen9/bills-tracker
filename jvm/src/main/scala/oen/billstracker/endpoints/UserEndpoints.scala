package oen.billstracker.endpoints

import cats.effect.Effect
import org.http4s.dsl.Http4sDsl

import cats.effect._
import org.http4s._
import org.http4s.server._
import org.http4s.circe._
import oen.billstracker.shared.Dto.User
import io.circe.syntax._
import io.circe.generic.auto._
import oen.billstracker.model.StorageData.DbUser
import io.scalaland.chimney.dsl._

class UserEndpoints[F[_] : Effect](authMiddleware: AuthMiddleware[F, DbUser]) extends Http4sDsl[F] {

  val authedEndpoints: AuthedService[DbUser, F] = AuthedService {
    case GET -> Root / "user" as user => Ok(user.into[User].transform.asJson)
  }
  val endpoints: HttpRoutes[F] = authMiddleware(authedEndpoints)
}

object UserEndpoints {
  def apply[F[_] : Effect](authMiddleware: AuthMiddleware[F, DbUser]): UserEndpoints[F] = new UserEndpoints[F](authMiddleware)
}

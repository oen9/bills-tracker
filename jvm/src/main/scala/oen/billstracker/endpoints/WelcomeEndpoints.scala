package oen.billstracker.endpoints

import cats.effect.Effect
import org.http4s.dsl.Http4sDsl

import cats.effect._
import org.http4s._
import org.http4s.server._
import oen.billstracker.model.StorageData.DbUser

class WelcomeEndpoints[F[_] : Effect](authMiddleware: AuthMiddleware[F, DbUser]) extends Http4sDsl[F] {

  val welcomeEndpoints: AuthedService[DbUser, F] = AuthedService {
    case GET -> Root / "welcome" as user => Ok(s"Welcome, ${user.name}")
    case GET -> Root / "welcome2" as user => Ok(user.toString)
  }

  val endpoints: HttpRoutes[F] = authMiddleware(welcomeEndpoints)
}

object WelcomeEndpoints {
  def apply[F[_] : Effect](authMiddleware: AuthMiddleware[F, DbUser]): WelcomeEndpoints[F] = new WelcomeEndpoints[F](authMiddleware)
}

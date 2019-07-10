package oen.billstracker.endpoints

import cats.effect.Effect
import org.http4s.dsl.Http4sDsl

import cats.effect._, cats.implicits._, cats.data._
import org.http4s._
import org.http4s.server._
import org.http4s.util.CaseInsensitiveString
import oen.billstracker.shared.Dto._
import oen.billstracker.services.AuthService
import org.http4s.circe._
import io.circe.generic.extras.auto._
import io.circe.syntax._
import oen.billstracker.model.StorageData.DbUser

trait AuthEndpoints[F[_]] extends Http4sDsl[F] {
  def authMiddleware: AuthMiddleware[F, DbUser]
  def endpoints: HttpRoutes[F]
}

class AuthEndpointsImpl[F[_] : Effect](authService: AuthService[F]) extends Http4sDsl[F] with AuthEndpoints[F] {

  private[this] val authUser: Kleisli[F, Request[F], Either[String, DbUser]] = Kleisli({ request =>
    val token = for {
      header <- request.headers.get(CaseInsensitiveString("Authorization")).toRight("header not found")
      token <- header.value.asRight[String]
    } yield token

    token.flatTraverse(authService.retrieveUser)
  })

  private[this] val onFailure: AuthedService[String, F] = Kleisli(req => OptionT.liftF(Forbidden(req.authInfo)))
  private[this] implicit val userDecoder = jsonOf[F, PlainUser]

  val authMiddleware = AuthMiddleware(authUser, onFailure)

  val endpoints: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "sign-in" => for {
      user <- req.as[PlainUser]
      result <- authService.signIn(user)
      resp <- result.fold(Effect[F].pure(Response[F](Status.Unauthorized)): F[Response[F]])(token => Ok(token.asJson))
    } yield resp

    case req @ POST -> Root / "sign-up" => for {
      user <- req.as[PlainUser]
      result <- authService.signUp(user)
      resp <- result match {
        case ErrorResponse(error) => BadRequest(error)
        case SuccessResponse => Created()
      }
    } yield resp
  }

}

object AuthEndpoints {
  def apply[F[_] : Effect](authService: AuthService[F]): AuthEndpoints[F] = new AuthEndpointsImpl[F](authService)
}

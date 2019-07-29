package oen.billstracker.services

import cats.effect.Effect
import cats.implicits._
import org.reactormonk.PrivateKey
import org.reactormonk.CryptoBits
import java.time.Clock
import oen.billstracker.model.StorageData._
import oen.billstracker.shared.Dto._
import org.mindrot.jbcrypt.BCrypt
import cats.data.OptionT

trait AuthService[F[_]] {
  def signIn(pu: PlainUser): F[Option[AuthToken]]
  def signUp(pu: PlainUser): F[ResponseCode]
  def retrieveUser(token: String): F[Either[String, DbUser]]
}

class AuthServiceImpl[F[_] : Effect](secret: String, mongoService: MongoService[F]) extends AuthService[F] {
  val key = PrivateKey(scala.io.Codec.toUTF8(secret))
  val crypto = CryptoBits(key)
  val clock = Clock.systemUTC()

  override def signIn(pu: PlainUser): F[Option[AuthToken]] = {
    val res: OptionT[F, AuthToken] = for {
      user <- OptionT(mongoService.getUserByName(pu.name)) if BCrypt.checkpw(pu.password, user.password)
      newToken = generateToken(user.name)
      _ <- OptionT(mongoService.updateToken(newToken.token, user))
    } yield newToken

    res.value
  }

  override def signUp(pu: PlainUser): F[ResponseCode] = for {
    _ <- Effect[F].unit
    encryptedPu = pu.copy(password = BCrypt.hashpw(pu.password, BCrypt.gensalt()))
    wRes <- mongoService.createUser(encryptedPu)
    resp = wRes.fold(ErrorResponse("Can't create user. Probably user exists."): ResponseCode)(_ => SuccessResponse)
  } yield resp

  override def retrieveUser(token: String): F[Either[String, DbUser]] = for {
    user <- mongoService.getUserByToken(token)
  } yield user.toRight("invalid token")

  private[this] def generateToken(s: String): AuthToken = {
    AuthToken(crypto.signToken(s, clock.millis.toString))
  }

}

object AuthService {
  def apply[F[_] : Effect](secret: String, mongoService: MongoService[F]): AuthService[F] = new AuthServiceImpl[F](secret, mongoService)
}

package oen.billstracker.services

import cats.effect.Effect
import cats.implicits._
import org.reactormonk.PrivateKey
import org.reactormonk.CryptoBits
import java.time.Clock
import oen.billstracker.model.StorageData.DbUser
import oen.billstracker.shared.Dto._
import org.mindrot.jbcrypt.BCrypt
import oen.billstracker.shared.Dto

trait AuthService[F[_]] {
  def signIn(pu: PlainUser): F[Option[AuthToken]]
  def signUp(pu: PlainUser): F[ResponseCode]
  def retrieveUser(token: String): F[Either[String, DbUser]]
}

class AuthServiceImpl[F[_] : Effect](secret: String) extends AuthService[F] {
  var tmpUsers: IndexedSeq[DbUser] = IndexedSeq(DbUser(name = "test", password = BCrypt.hashpw("test", BCrypt.gensalt()),token = "test"))

  val key = PrivateKey(scala.io.Codec.toUTF8(secret))
  val crypto = CryptoBits(key)
  val clock = Clock.systemUTC()

  override def signIn(pu: PlainUser): F[Option[AuthToken]] = {
    val result = tmpUsers
      .filter(tu => tu.name == pu.name && BCrypt.checkpw(pu.password, tu.password))
      .headOption
      .map(founded => {
        val newToken = generateToken(founded.name)
        val index = tmpUsers.indexOf(founded)
        val updatedUser = founded.copy(token = newToken.token)
        tmpUsers = tmpUsers.updated(index, updatedUser)
        newToken
      })
    Effect[F].pure(result)
  }

  override def signUp(pu: Dto.PlainUser): F[ResponseCode] = {
    val result = tmpUsers
      .filter(_.name == pu.name)
      .headOption
      .fold{
        val hashedPw = BCrypt.hashpw(pu.password, BCrypt.gensalt())
        tmpUsers = tmpUsers :+ DbUser(name = pu.name, password = hashedPw)
        SuccessResponse : ResponseCode
      }(_ => ErrorResponse("User exists"))
    Effect[F].pure(result)
  }

  override def retrieveUser(token: String): F[Either[String, DbUser]] = {
    tmpUsers
      .filter(_.token == token)
      .headOption.toRight("invalid token")
      .traverse(Effect[F].pure)
  }

  private[this] def generateToken(s: String): AuthToken = {
    AuthToken(crypto.signToken(s, clock.millis.toString))
  }

}

object AuthService {
  def apply[F[_] : Effect](secret: String): AuthService[F] = new AuthServiceImpl[F](secret)
}
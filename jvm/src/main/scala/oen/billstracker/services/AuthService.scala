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
import org.log4s.getLogger
import org.billstracker.utils.LogsHelper

trait AuthService[F[_]] {
  def signIn(pu: PlainUser): F[Option[AuthToken]]
  def signUp(pu: PlainUser): F[ResponseCode]
  def retrieveUser(token: String): F[Either[String, DbUser]]
}

class AuthServiceImpl[F[_] : Effect](secret: String, mongoService: MongoService[F]) extends AuthService[F] {
  private[this] implicit val logger = getLogger(getClass)
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

  def initTestUser: F[Unit] = {
    import AuthService.TEST_USER

    def createTestToken(dbUser: DbUser): F[Unit] = for {
      _ <- mongoService.updateToken(TEST_USER.password, dbUser)
    } yield ()

    def createTestUser: F[Unit] = for {
      _ <- signUp(TEST_USER)
      maybeTestUser <- mongoService.getUserByName(TEST_USER.name)
      _ <- maybeTestUser.fold(LogsHelper.error("Can't create test user."))(createTestToken)
      _ <- LogsHelper.info("Test user created.")
    } yield ()

    for {
      _ <- Effect[F].unit
      testUser <- mongoService.getUserByName(TEST_USER.name)
      _ <- testUser.fold(createTestUser)(_ => LogsHelper.info("Test user exists."))
    } yield ()
  }

  private[this] def generateToken(s: String): AuthToken = {
    import AuthService.TEST_USER
    val tokenValue = if (s == TEST_USER.name) TEST_USER.password
    else crypto.signToken(s, clock.millis.toString)
    AuthToken(tokenValue)
  }

}

object AuthService {
  def apply[F[_] : Effect](secret: String, mongoService: MongoService[F]): F[AuthService[F]] = for {
    _ <- Effect[F].unit
    authService = new AuthServiceImpl[F](secret, mongoService)
    _ <- authService.initTestUser
  } yield authService

  val TEST_USER = PlainUser("test", "test")
}

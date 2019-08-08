package oen.billstracker.services

import cats.effect.{Effect, Resource}
import cats.implicits._
import oen.billstracker.model.StorageData._
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}

import scala.concurrent.ExecutionContext
import oen.billstracker.shared.Dto.PlainUser
import reactivemongo.api.indexes.Index
import reactivemongo.api.indexes.IndexType
import reactivemongo.api.FailoverStrategy
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.commands.UpdateWriteResult
import reactivemongo.bson.BSONObjectID

trait MongoService[F[_]] {
  def createUser(pu: PlainUser): F[Option[WriteResult]]
  def getUserByName(name: String): F[Option[DbUser]]
  def getUserByToken(token: String): F[Option[DbUser]]

  def updateToken(token: String, dbUser: DbUser): F[Option[UpdateWriteResult]]

  def addGroup(dbUser: DbUser, group: DbBillGroup): F[Option[UpdateWriteResult]]
  def deleteItem(dbUser: DbUser, groupId: BSONObjectID, itemId: BSONObjectID): F[Option[UpdateWriteResult]]
  def addItem(dbUser: DbUser, groupId: BSONObjectID, item: DbBillItem): F[Option[UpdateWriteResult]]
  def updateItem(dbUser: DbUser, groupId: BSONObjectID, item: DbBillItem): F[Option[UpdateWriteResult]]
}

object MongoService {
  def apply[F[_] : Effect](mongoUri: String)(implicit dbEc: ExecutionContext): Resource[F, MongoService[F]] = {
    import oen.billstracker.utils.LiftAny._

    def connectToDb(driver: MongoDriver): F[DefaultDB] = for {
      uri <- Effect[F].fromTry(MongoConnection.parseURI(mongoUri))
      con <- Effect[F].fromTry(driver.connection(uri, true))
      dn <- Effect[F].fromOption(uri.db, new Exception("cannot get db from uri"))
      db <- con.database(dn, FailoverStrategy(retries = 20)).toF // (retries = 20) == 32 seconds
    } yield db

    def createMongoService(db: DefaultDB) = {
      val dbUsers = db.collection(USERS_COLLECTION_NAME): BSONCollection
      for {
        _ <- dbUsers.indexesManager.ensure(Index(key = Seq("name" -> IndexType.Ascending), unique = true)).toF
      } yield MongoServiceImpl[F](dbUsers, dbEc)
    }

    for {
      driver <- Resource.make(Effect[F].delay(MongoDriver()))(driver => Effect[F].delay(driver.close()))
      db <- Resource.liftF(connectToDb(driver))
      mongoService <- Resource.liftF(createMongoService(db))
    } yield mongoService
  }
}

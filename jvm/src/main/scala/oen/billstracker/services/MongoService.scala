package oen.billstracker.services

import cats.effect.{Effect, Resource}
import cats.implicits._
import oen.billstracker.model.StorageData._
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}

import scala.concurrent.ExecutionContext

trait MongoService[F[_]] {
}

object MongoService {
  def apply[F[_] : Effect](mongoUri: String)(implicit dbEc: ExecutionContext): Resource[F, MongoService[F]] = {
    import oen.billstracker.tclass.LiftAny._

    def connectToDb(driver: MongoDriver): F[DefaultDB] = for {
      uri <- Effect[F].fromTry(MongoConnection.parseURI(mongoUri))
      con <- Effect[F].fromTry(driver.connection(uri, true))
      dn <- Effect[F].pure(uri.db.get)
      db <- con.database(dn).toF
    } yield db

    def createMongoService(db: DefaultDB) = {
      val dbUsers = db.collection(USERS_COLLECTION_NAME): BSONCollection
      MongoServiceImpl[F](dbUsers, dbEc)
    }

    for {
      driver <- Resource.make(Effect[F].delay(MongoDriver()))(driver => Effect[F].delay(driver.close()))
      db <- Resource.liftF(connectToDb(driver))
      mongoService = createMongoService(db)
    } yield mongoService
  }
}

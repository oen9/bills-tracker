package oen.billstracker.services

import cats.implicits._
import cats.effect.Effect
import reactivemongo.api.collections.bson.BSONCollection
import scala.concurrent.ExecutionContext
import oen.billstracker.tclass.LiftAny._
import oen.billstracker.shared.Dto._
import reactivemongo.api.commands.WriteResult
import oen.billstracker.model.StorageData.DbUser
import reactivemongo.bson.BSONDocument
import reactivemongo.api.commands.UpdateWriteResult

class MongoServiceImpl[F[_] : Effect](dbUsers: BSONCollection, implicit val dbEc: ExecutionContext) extends MongoService[F] {
  override def createUser(pu: PlainUser): F[Option[WriteResult]] = for {
    _ <- Effect[F].unit
    dbU = DbUser(name = pu.name, password = pu.password)
    wRes <- dbUsers.insert.one(dbU).toF
            .map(_.some)
            .handleError(_ => None) // TODO log error
  } yield (wRes)

  override def getUserByName(name: String): F[Option[DbUser]] = for {
    _ <- Effect[F].unit
    query = BSONDocument("name" -> name)
    user <- dbUsers.find(query, Option.empty).one[DbUser].toF
  } yield user

  def updateToken(token: String, dbUser: DbUser): F[Option[UpdateWriteResult]] = for {
    _ <- Effect[F].unit
    query = BSONDocument("_id" -> dbUser._id)
    upd = BSONDocument("$set" -> BSONDocument("token" -> token))
    res <- dbUsers.update.one(query, upd).toF
            .map(_.some)
            .handleError(_ => None)
  } yield res

  override def getUserByToken(token: String): F[Option[DbUser]] = for {
    _ <- Effect[F].unit
    query = BSONDocument("token" -> token)
    user <- dbUsers.find(query, Option.empty).one[DbUser].toF
  } yield user
}

object MongoServiceImpl {
  def apply[F[_] : Effect](dbUsers: BSONCollection, dbEc: ExecutionContext): MongoServiceImpl[F] =
    new MongoServiceImpl(dbUsers, dbEc)
}

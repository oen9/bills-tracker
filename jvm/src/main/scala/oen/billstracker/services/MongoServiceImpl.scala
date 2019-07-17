package oen.billstracker.services

import cats.effect.Effect
import reactivemongo.api.collections.bson.BSONCollection
import scala.concurrent.ExecutionContext

class MongoServiceImpl[F[_] : Effect](dbUsers: BSONCollection, implicit val dbEc: ExecutionContext) extends MongoService[F] {
}

object MongoServiceImpl {
  def apply[F[_] : Effect](dbUsers: BSONCollection, dbEc: ExecutionContext): MongoServiceImpl[F] =
    new MongoServiceImpl(dbUsers, dbEc)
}

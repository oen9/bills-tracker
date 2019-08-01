package oen.billstracker.services

import cats.effect.Effect
import cats.implicits._
import oen.billstracker.model.StorageData._
import _root_.reactivemongo.bson.BSONObjectID

trait GroupsService[F[_]] {
  def addGroup(user: DbUser, newGroup: DbBillGroup): F[Option[DbBillGroup]]
}

class GroupsServiceImpl[F[_] : Effect](mongoService: MongoService[F]) extends GroupsService[F] {

  def addGroup(user: DbUser, newGroup: DbBillGroup): F[Option[DbBillGroup]] = for {
    _ <- Effect[F].unit
    filledGroup = newGroup.copy(id = BSONObjectID.generate().some)
    wRes <- mongoService.addGroup(user, filledGroup)
    resp = wRes.map(_ => filledGroup)
  } yield resp
}

object GroupsService {
  def apply[F[_] : Effect](mongoService: MongoService[F]): GroupsService[F] = new GroupsServiceImpl[F](mongoService)
}

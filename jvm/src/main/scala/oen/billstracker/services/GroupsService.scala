package oen.billstracker.services

import cats.effect.Effect
import cats.implicits._
import oen.billstracker.model.StorageData._
import _root_.reactivemongo.bson.BSONObjectID
import oen.billstracker.shared.Dto.SuccessResponse
import akka.io.dns.internal.ResponseCode
import akka.io.dns.internal.ResponseCode
import oen.billstracker.shared.Dto.ResponseCode

trait GroupsService[F[_]] {
  def addGroup(user: DbUser, newGroup: DbBillGroup): F[Option[DbBillGroup]]
  def deleteItem(user: DbUser, groupId: BSONObjectID, itemId: BSONObjectID): F[Option[ResponseCode]]
}

class GroupsServiceImpl[F[_] : Effect](mongoService: MongoService[F]) extends GroupsService[F] {

  def addGroup(user: DbUser, newGroup: DbBillGroup): F[Option[DbBillGroup]] = for {
    _ <- Effect[F].unit
    filledGroup = newGroup.copy(id = BSONObjectID.generate().some)
    wRes <- mongoService.addGroup(user, filledGroup)
    resp = wRes.map(_ => filledGroup)
  } yield resp

  def deleteItem(user: DbUser, groupId: BSONObjectID, itemId: BSONObjectID): F[Option[ResponseCode]] = for {
    _ <- Effect[F].unit
    wRes <- mongoService.deleteItem(user, groupId, itemId)
    resp = wRes.map(_ => SuccessResponse)
  } yield resp
}

object GroupsService {
  def apply[F[_] : Effect](mongoService: MongoService[F]): GroupsService[F] = new GroupsServiceImpl[F](mongoService)
}

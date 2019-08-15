package oen.billstracker.services

import cats.effect.Effect
import cats.implicits._
import oen.billstracker.model.StorageData._
import _root_.reactivemongo.bson.BSONObjectID
import oen.billstracker.shared.Dto.SuccessResponse
import akka.io.dns.internal.ResponseCode
import akka.io.dns.internal.ResponseCode
import oen.billstracker.shared.Dto.ResponseCode
import java.{util => ju}

trait GroupsService[F[_]] {
  def addGroup(user: DbUser, newGroup: DbBillGroup): F[Option[DbBillGroup]]
  def deleteItem(user: DbUser, groupId: BSONObjectID, itemId: BSONObjectID): F[Option[ResponseCode]]
  def addItem(user: DbUser, groupId: BSONObjectID): F[Option[DbBillItem]]
  def updateItem(user: DbUser, groupId: BSONObjectID, dbBillItem: DbBillItem): F[Option[ResponseCode]]
  def updateGroupName(user: DbUser, groupId: BSONObjectID, name: String): F[Option[ResponseCode]]
  def deleteGroup(user: DbUser, groupId: BSONObjectID): F[Option[ResponseCode]]
}

class GroupsServiceImpl[F[_] : Effect](mongoService: MongoService[F]) extends GroupsService[F] {

  def addGroup(user: DbUser, newGroup: DbBillGroup): F[Option[DbBillGroup]] = for {
    _ <- Effect[F].unit
    filledGroup = newGroup.copy(id = BSONObjectID.generate().some)
    wRes <- mongoService.addGroup(user, filledGroup)
    resp = wRes.map(_ => filledGroup)
  } yield resp

  def deleteItem(user: DbUser, groupId: BSONObjectID, itemId: BSONObjectID): F[Option[ResponseCode]] = for {
    wRes <- mongoService.deleteItem(user, groupId, itemId)
    resp = wRes.map(_ => SuccessResponse)
  } yield resp

  def addItem(user: DbUser, groupId: BSONObjectID): F[Option[DbBillItem]] = for {
    _ <- Effect[F].unit
    newBillItem = DbBillItem(id = BSONObjectID.generate().some, date = new ju.Date)
    wRes <- mongoService.addItem(user, groupId, newBillItem)
    resp = wRes.map(_ => newBillItem)
  } yield resp

  def updateItem(user: DbUser, groupId: BSONObjectID, dbBillItem: DbBillItem): F[Option[ResponseCode]] = for {
    wRes <- mongoService.updateItem(user, groupId, dbBillItem)
    resp = wRes.map(_ => SuccessResponse)
  } yield resp

  def updateGroupName(user: DbUser, groupId: BSONObjectID, name: String): F[Option[ResponseCode]] = for {
    wRes <- mongoService.updateGroupName(user, groupId, name)
    resp = wRes.map(_ => SuccessResponse)
  } yield resp

  def deleteGroup(user: DbUser, groupId: BSONObjectID): F[Option[ResponseCode]] = for {
    wRes <- mongoService.deleteGroup(user, groupId)
    resp = wRes.map(_ => SuccessResponse)
  } yield resp
}

object GroupsService {
  def apply[F[_] : Effect](mongoService: MongoService[F]): GroupsService[F] = new GroupsServiceImpl[F](mongoService)
}

package oen.billstracker.endpoints

import cats.effect.Effect
import org.http4s.dsl.Http4sDsl

import cats.implicits._
import cats.effect._
import org.http4s._
import org.http4s.server._
import org.http4s.circe._
import oen.billstracker.shared.Dto._
import io.circe.syntax._
import io.circe.generic.extras.auto._
import oen.billstracker.model.StorageData._
import io.scalaland.chimney.dsl._
import oen.billstracker.services.GroupsService
import reactivemongo.bson.BSONObjectID

class GroupsEndpoints[F[_] : Effect](
  authMiddleware: AuthMiddleware[F, DbUser],
  groupsService: GroupsService[F]
) extends Http4sDsl[F] {
  private[this] implicit val billGroupDecoder = jsonOf[F, BillGroup]
  private[this] implicit val billItemDecoder = jsonOf[F, BillItem]

  val authedEndpoints: AuthedService[DbUser, F] = AuthedService {
    case authReq @ POST -> Root as user => for {
      billGroup <- authReq.req.as[BillGroup]
      dbBillGroup = billGroup.into[DbBillGroup].transform
      maybeAddedDbGroup <- groupsService.addGroup(user, dbBillGroup)
      maybeAddedGroup = maybeAddedDbGroup.map(_.into[BillGroup].transform)
      rr <- maybeAddedGroup.fold(BadRequest("Can't create group"))(addedGroup => Created(addedGroup.asJson))
    } yield rr

    case authReq @ DELETE -> Root / groupId / "items" / itemId as user => for {
      _ <- Effect[F].unit
      groupDbId = BSONObjectID.parse(groupId).getOrElse(BSONObjectID.generate())
      itemDbId = BSONObjectID.parse(itemId).getOrElse(BSONObjectID.generate())
      deleteResult <- groupsService.deleteItem(user, groupDbId, itemDbId)
      rr <- deleteResult.fold(BadRequest("Item not found"))(_ => Ok())
    } yield rr

    case authReq @ POST -> Root / groupId / "items" as user => for {
      _ <- Effect[F].unit
      groupDbId = BSONObjectID.parse(groupId).getOrElse(BSONObjectID.generate())
      addItemResult <- groupsService.addItem(user, groupDbId)
      maybeAddedItem = addItemResult.map(_.into[BillItem].transform)
      rr <- maybeAddedItem.fold(BadRequest("Can't create item"))(addedItem => Created(addedItem.asJson))
    } yield rr

    case authReq @ PUT -> Root / groupId / "items" / itemId as user => for {
      billItem <- authReq.req.as[BillItem]
      groupDbId = BSONObjectID.parse(groupId).getOrElse(BSONObjectID.generate())
      itemDbId = BSONObjectID.parse(itemId).getOrElse(BSONObjectID.generate())
      dbBillItem = billItem.into[DbBillItem]
        .withFieldComputed(_.id, _ => itemDbId.some)
        .transform

      maybeSuccess <- groupsService.updateItem(user, groupDbId, dbBillItem)
      rr <- maybeSuccess.fold(BadRequest("Can't update item"))(_ => NoContent())
    } yield rr

    case authReq @ PUT -> Root / groupId as user => for {
      billGroup <- authReq.req.as[BillGroup]
      groupDbId = BSONObjectID.parse(groupId).getOrElse(BSONObjectID.generate())
      maybeSuccess <- groupsService.updateGroupName(user, groupDbId, billGroup.name)
      rr <- maybeSuccess.fold(BadRequest("Can't update group"))(_ => NoContent())
    } yield rr
  }
  val endpoints: HttpRoutes[F] = authMiddleware(authedEndpoints)
}

object GroupsEndpoints {
  def apply[F[_] : Effect](
    authMiddleware: AuthMiddleware[F, DbUser],
    groupsService: GroupsService[F]
  ): GroupsEndpoints[F] = new GroupsEndpoints[F](authMiddleware, groupsService)
}

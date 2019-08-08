package oen.billstracker.services.handlers

import diode.{ActionHandler, ModelRW}
import oen.billstracker.services.WebData._
import oen.billstracker.shared.Dto.BillGroup
import diode.Effect
import scala.concurrent.ExecutionContext.Implicits.global
import com.softwaremill.quicklens._
import cats.implicits._

class ItemsHandler[M](modelRW: ModelRW[M, Option[IndexedSeq[BillGroup]]]) extends ActionHandler(modelRW) {

  override def handle = {
    case DeleteItemA(token, groupId, itemId) =>
      println(s"$itemId ready to delete in $groupId group")
      val deleteItem = Effect(AjaxClient.deleteItem(token, groupId, itemId).map(_ => ItemDeletedA(groupId, itemId)))
      effectOnly(deleteItem)

    case ItemDeletedA(groupId, itemId) =>
    // updated(value.modify(_.each.eachWhere(_.id == groupId.some).items).using(_.filter(_.id != itemId.some))) // compile error (can't use groupId inside eachWhere)
    updated(
      value.modify(_.each).using(_.map { group =>
        if (group.id == groupId.some) group.modify(_.items).using(_.filter(_.id != itemId.some))
        else group
      })
    )
  }
}

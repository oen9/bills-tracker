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
      val deleteItem = Effect(AjaxClient.deleteItem(token, groupId, itemId).map(_ => ItemDeletedA(groupId, itemId)))
      effectOnly(deleteItem)

    case ItemDeletedA(groupId, itemId) =>
      // updated(value.modify(_.each.eachWhere(_.id == groupId.some).items).using(_.filter(_.id != itemId.some))) // compile error (can't use groupId inside eachWhere)
      val newValue = modGroup(value, groupId)(_.modify(_.items).using(_.filter(_.id != itemId.some)))
      updated(newValue)

    case AddNewItemA(token, groupId) =>
      val newItem = Effect(AjaxClient.addItem(token, groupId).map(NewItemAddedA(groupId, _)))
      effectOnly(newItem)

    case NewItemAddedA(groupId, billItem) =>
      val newValue = modGroup(value, groupId)(_.modify(_.items).using(_ :+ billItem))
      updated(newValue)

    case UpdateItemA(token, groupId, itemId, item) =>
      val updateItem = Effect(AjaxClient.updateItem(token, groupId, itemId, item).map(_ => ItemUpdatedA(groupId, itemId, item)))
      effectOnly(updateItem)

    case ItemUpdatedA(groupId, itemId, item) =>
      val newValue = modGroup(value, groupId)(_.modify(_.items).using(_.map { oldItem =>
        if (oldItem.id == itemId.some) item
        else oldItem
      }))
      updated(newValue)

  }

  def modGroup(value: Option[IndexedSeq[BillGroup]], groupId: String)(op: BillGroup => BillGroup): Option[IndexedSeq[BillGroup]] = {
    value.modify(_.each).using(_.map { group =>
      if (group.id == groupId.some) op(group)
      else group
    })
  }
}

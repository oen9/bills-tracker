package oen.billstracker.services.handlers

import diode.{ActionHandler, ModelRW}
import oen.billstracker.services.WebData._
import oen.billstracker.shared.Dto.BillGroup
import com.softwaremill.quicklens._
import cats.implicits._
import diode.Effect
import scala.concurrent.ExecutionContext.Implicits.global

class GroupsHandler[M](modelRW: ModelRW[M, Option[IndexedSeq[BillGroup]]]) extends ActionHandler(modelRW) {

  override def handle = {
    case NewGroupAddedA(newGroup) =>
      println("new group: " + newGroup)
      updated(value.map(newGroup +: _))

    case UpdateGroupNameA(token, groupId, name) =>
      val updateGroupName = Effect(AjaxClient.updateGroupName(token, groupId, BillGroup(name)).map(_ => GroupNameUpdatedA(groupId, name)))
      effectOnly(updateGroupName)

    case GroupNameUpdatedA(groupId, name) =>
      val newValue = value.modify(_.each).using(_.map { group =>
        if (group.id == groupId.some) group.modify(_.name).setTo(name)
        else group
      })
      updated(newValue)
  }
}

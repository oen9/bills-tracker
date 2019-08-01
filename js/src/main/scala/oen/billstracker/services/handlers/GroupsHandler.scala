package oen.billstracker.services.handlers

import diode.{ActionHandler, ModelRW}
import oen.billstracker.services.WebData._
import oen.billstracker.shared.Dto.BillGroup

class GroupsHandler[M](modelRW: ModelRW[M, Option[IndexedSeq[BillGroup]]]) extends ActionHandler(modelRW) {

  override def handle = {
    case NewGroupAddedA(newGroup) =>
      println("new group: " + newGroup)
      updated(value.map(newGroup +: _))
  }
}

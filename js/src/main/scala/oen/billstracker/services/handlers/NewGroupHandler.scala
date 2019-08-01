package oen.billstracker.services.handlers

import diode.{ActionHandler, ModelRW}
import diode.data._
import scala.concurrent.ExecutionContext.Implicits.global
import oen.billstracker.services.WebData._
import diode.NoAction
import diode.Action
import oen.billstracker.shared.Dto.BillGroup
import oen.billstracker.shared.Dto.AddNewGroup

class NewGroupHandler[M](modelRW: ModelRW[M, Pot[BillGroup]]) extends ActionHandler(modelRW) {
  override def handle = {
    case action: TryAddNewGroup =>
      val data = AddNewGroup(action.newGroupName)
      val updateF = action.effect(AjaxClient.addNewGroup(action.token, data))(identity)

      val onReady: PotAction[BillGroup, TryAddNewGroup] => Action = _.potResult.fold(NoAction: Action)(NewGroupAddedA(_))
      action.handleWith(this, updateF)(GenericHandlers.withOnReady(onReady))

    case CleanPotA =>
      updated(Empty)
  }
}

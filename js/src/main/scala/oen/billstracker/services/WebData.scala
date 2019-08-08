package oen.billstracker.services

import diode.Action
import diode.data.Pot
import diode.data.Empty
import diode.data.PotAction
import oen.billstracker.shared.Dto.User
import oen.billstracker.shared.Dto.BillGroup
import oen.billstracker.shared.Dto.BillItem

object WebData {
  case class Clicks(count: Int)
  case class Pots(newGroupResult: Pot[BillGroup] = Empty)
  case class Me(name: String, token: String, clicks: Clicks = Clicks(0))
  case class SignModel(potResult: Pot[String] = Empty)
  case class RootModel(
    me: Option[Me] = None,
    signModel: SignModel = SignModel(),
    user: Option[User] = None,
    pots: Pots = Pots()
  )

  case class SignedInA(username: String, token: String) extends Action
  case class SignedUpA(username: String) extends Action
  case object SignOutA extends Action
  case object CleanPotA extends Action
  case object GetUserData extends Action
  case class GotUserDataA(u: User) extends Action
  case class NewGroupAddedA(group: BillGroup) extends Action

  case class DeleteItemA(token: String, billGroupId: String, itemId: String) extends Action
  case class ItemDeletedA(billGroupId: String, itemId: String) extends Action

  case class AddNewItemA(token: String, billGroupId: String) extends Action
  case class NewItemAddedA(billgroupId: String, billItem: BillItem) extends Action

  case class UpdateItemA(token: String, billGroupId: String, billItemId: String, billItem: BillItem) extends Action
  case class ItemUpdatedA(billGroupId: String, billItemId: String, billItem: BillItem) extends Action

  case object IncreaseClicks extends Action // TODO remove it

  case class TrySignUp(username: String, password: String, potResult: Pot[String] = Empty) extends PotAction[String, TrySignUp] {
    def next(newResult: Pot[String]) = copy(potResult = newResult)
  }

  case class TrySignIn(username: String, password: String, potResult: Pot[String] = Empty) extends PotAction[String, TrySignIn] {
    def next(newResult: Pot[String]) = copy(potResult = newResult)
  }

  case class TryAddNewGroup(token: String, newGroupName: String, potResult: Pot[BillGroup] = Empty) extends PotAction[BillGroup, TryAddNewGroup] {
    def next(newResult: Pot[BillGroup]) = copy(potResult = newResult)
  }
}

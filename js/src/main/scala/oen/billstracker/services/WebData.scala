package oen.billstracker.services

import diode.Action
import diode.data.Pot
import diode.data.Empty
import diode.data.PotAction

object WebData {
  case class Clicks(count: Int)
  case class Me(name: String, token: String, clicks: Clicks = Clicks(0))
  case class SignModel(potResult: Pot[String] = Empty)
  case class RootModel(me: Option[Me] = None, signModel: SignModel = SignModel())

  case class SignedInA(username: String, token: String) extends Action
  case class SignedUpA(username: String) extends Action
  case object SignOutA extends Action
  case object CleanPotA extends Action
  case object IncreaseClicks extends Action // TODO remove it

  case class TrySignUp(username: String, password: String, potResult: Pot[String] = Empty) extends PotAction[String, TrySignUp] {
    def next(newResult: Pot[String]) = copy(potResult = newResult)
  }

  case class TrySignIn(username: String, password: String, potResult: Pot[String] = Empty) extends PotAction[String, TrySignIn] {
    def next(newResult: Pot[String]) = copy(potResult = newResult)
  }
}

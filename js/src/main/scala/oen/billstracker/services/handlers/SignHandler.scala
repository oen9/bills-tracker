package oen.billstracker.services.handlers

import diode.{ActionHandler, ModelRW}
import diode.data._
import scala.concurrent.ExecutionContext.Implicits.global
import oen.billstracker.services.WebData._
import diode.NoAction
import diode.Action
import oen.billstracker.shared.Dto.PlainUser

class GenericSignHandler[M](modelRW: ModelRW[M, Option[Me]]) extends ActionHandler(modelRW) {
  override def handle = {
    case SignInA(username, password) => println(username + " " + password); updated(Some(Me()))
    case SignOutA => updated(None)
    case SignedUpA(username) => println(s"Signed up: $username"); noChange
  }
}

class SignUpHandler[M](modelRW: ModelRW[M, Pot[String]]) extends ActionHandler(modelRW) {
  override def handle = {
    case action: TrySignUp =>
      val data = PlainUser(action.username, action.password)
      val updateF = action.effect(AjaxClient.signUp(data))(_ => data.name)

      val onReady: PotAction[String, TrySignUp] => Action = _.potResult.fold(NoAction: Action)(SignedUpA(_))
      action.handleWith(this, updateF)(GenericHandlers.withOnReady(onReady))
  }
}

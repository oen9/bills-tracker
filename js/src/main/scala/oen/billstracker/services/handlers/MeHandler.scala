package oen.billstracker.services.handlers

import diode.{ActionHandler, ModelRW}
import scala.concurrent.ExecutionContext.Implicits.global
import oen.billstracker.services.WebData._
import diode.Effect

class MeSignHandler[M](modelRW: ModelRW[M, Option[Me]]) extends ActionHandler(modelRW) {

  override def handle = {
    case SignedInA(username, token) =>
      println(s"$username signed in with token: $token")
      val getUserData = Effect(AjaxClient.getUserData(token).map(GotUserDataA))
      updated(Some(Me(username, token)), getUserData)

    case SignOutA => updated(None)

    case SignedUpA(username) => println(s"Signed up: $username"); noChange
  }
}

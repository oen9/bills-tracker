package oen.billstracker.services.handlers

import diode.{ActionHandler, ModelRW}
import diode.data._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Promise
import oen.billstracker.services.WebData._

class GenericSignHandler[M](modelRW: ModelRW[M, Option[Me]]) extends ActionHandler(modelRW) {
  override def handle = {
    case SignInA(username, password) => println(username + " " + password); updated(Some(Me()))
    case SignOutA => updated(None)
    case SignUpA(username, password) => println(s"registered $username $password"); noChange
  }
}

class SignUpHandler[M](modelRW: ModelRW[M, Pot[String]]) extends ActionHandler(modelRW) {
  override def handle = {
    case action: TrySignUp =>
      val updateF = action.effect({
        val p = Promise[String]()
        scala.scalajs.js.timers.setTimeout(1000) {
          p.success(s"${action.username} registered")
        }
        p.future
      }) (identity _)
      action.handleWith(this, updateF)(PotAction.handler())
  }
}

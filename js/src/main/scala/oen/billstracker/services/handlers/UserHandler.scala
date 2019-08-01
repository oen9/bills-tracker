package oen.billstracker.services.handlers

import diode.{ActionHandler, ModelRW}
import oen.billstracker.services.WebData._
import oen.billstracker.shared.Dto.User
import cats.implicits._
import com.softwaremill.quicklens._

class UserHandler[M](modelRW: ModelRW[M, Option[User]]) extends ActionHandler(modelRW) {
  override def handle = {
    case GotUserDataA(u) =>
      val withReversedGroups = u.modify(_.billsGroups).using(_.reverse)
      updated(withReversedGroups.some)
  }
}

package oen.billstracker.shared

import io.circe.generic.extras.Configuration

case object Dto {
  implicit val customConfig: Configuration = Configuration.default.withDefaults

  case class User(name: String, billsGroups: IndexedSeq[BillGroup] = IndexedSeq())
  case class BillGroup(id: Option[String] = None, name: String, items: IndexedSeq[BillItem] = IndexedSeq())
  case class BillItem(id: Option[String] = None, description: String, value: BigDecimal)

  case class PlainUser(name: String = "", password: String = "")
  case class AuthToken(token: String)

  sealed trait ResponseCode
  case object SuccessResponse extends ResponseCode
  case class ErrorResponse(error: String = "unknown error") extends ResponseCode
}

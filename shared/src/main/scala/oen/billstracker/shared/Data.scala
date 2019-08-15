package oen.billstracker.shared

import io.circe.generic.extras.Configuration
import java.{util => ju}
import io.circe.Encoder
import io.circe.Decoder
import io.circe.syntax._

case object Dto {
  implicit val customConfig: Configuration = Configuration.default.withDefaults

  case class User(name: String, billsGroups: IndexedSeq[BillGroup] = IndexedSeq())
  case class BillGroup(name: String, id: Option[String] = None, items: IndexedSeq[BillItem] = IndexedSeq())
  case class BillItem(description: String, id: Option[String] = None, value: BigDecimal = BigDecimal(0), date: ju.Date)

  case class PlainUser(name: String = "", password: String = "")
  case class AuthToken(token: String)

  case class AddNewGroup(name: String)

  sealed trait ResponseCode
  case object SuccessResponse extends ResponseCode
  case class ErrorResponse(error: String = "unknown error") extends ResponseCode

  implicit val dateEncoder: Encoder[ju.Date] = Encoder.instance(a => a.getTime.asJson)
  implicit val dateDecoder: Decoder[ju.Date] = Decoder.instance(a => a.as[Long].map(new ju.Date(_)))
}

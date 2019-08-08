package oen.billstracker.services.handlers
import oen.billstracker.shared.Dto._
import io.circe.syntax._
import io.circe.generic.extras.auto._
import io.circe.parser.decode
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.ext.AjaxException
import io.circe.Decoder
import scala.util.Try
import org.scalajs.dom.raw.XMLHttpRequest
import scala.util.Failure
import scala.util.Success

object AjaxClient {
  val JSON_TYPE = Map("Content-Type" -> "application/json")
  def authHeader(token: String) = "Authorization" -> token
  def authJsonType(token: String) = JSON_TYPE + ("Authorization" -> token)

  import scala.concurrent.ExecutionContext.Implicits.global

  def signUp(user: PlainUser) = {
    Ajax.post(
      url = "/sign-up",
      data = user.asJson.noSpaces,
      headers = JSON_TYPE
    ).transform(_.responseText, onFailure)
  }

  def signIn(user: PlainUser) = {
    Ajax.post(
      url = "/sign-in",
      data = user.asJson.noSpaces,
      headers = JSON_TYPE
    ).transform(decodeAndHandleErrors[AuthToken])
  }

  def getUserData(token: String) = {
    Ajax.get(
      url = "/user",
      headers = JSON_TYPE + authHeader(token)
    ).transform(decodeAndHandleErrors[User])
  }

  def addNewGroup(token: String, data: AddNewGroup) = {
    Ajax.post(
      url = "/groups",
      data = data.asJson.noSpaces,
      headers = JSON_TYPE + authHeader(token)
    ).transform(decodeAndHandleErrors[BillGroup])
  }

  def deleteItem(token: String, groupId: String, itemId: String) = {
    Ajax.delete(
      url = s"/groups/$groupId/items/$itemId",
      headers = JSON_TYPE + authHeader(token)
    ).transform(_.responseText, onFailure)
  }

  def addItem(token: String, groupId: String) = {
    Ajax.post(
      url = s"/groups/$groupId/items",
      headers = JSON_TYPE + authHeader(token)
    ).transform(decodeAndHandleErrors[BillItem])
  }

  def updateItem(token: String, groupId: String, itemId: String, data: BillItem) = {
    Ajax.put(
      url = s"/groups/$groupId/items/$itemId",
      data = data.asJson.noSpaces,
      headers = JSON_TYPE + authHeader(token)
    ).transform(_.responseText, onFailure)
  }

  def updateGroupName(token: String, groupId: String, data: BillGroup) = {
    Ajax.put(
      url = s"/groups/$groupId",
      data = data.asJson.noSpaces,
      headers = JSON_TYPE + authHeader(token)
    ).transform(_.responseText, onFailure)
  }

  private[this] def decodeAndHandleErrors[A: Decoder](t: Try[XMLHttpRequest]): Try[A] = t match {
    case Success(req) => decode[A](req.responseText).toTry
    case Failure(e) => Failure(onFailure(e))
  }

  private[this] def onFailure: Throwable => Throwable = _ match {
    case ex: AjaxException => AjaxClient.ErrorWithMsgException(s"${ex.xhr.statusText}. ${ex.xhr.responseText}")
    case _ => AjaxClient.UnknownErrorException
  }

  case object UnknownErrorException extends Exception("unknown error")
  case class ErrorWithMsgException(s: String) extends Exception(s)
}

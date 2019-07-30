package oen.billstracker.services.handlers
import oen.billstracker.shared.Dto._
import io.circe.syntax._
import io.circe.generic.extras.auto._
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.ext.AjaxException

object AjaxClient {
  val JSON_TYPE = Map("Content-Type" -> "application/json")
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
    ).transform(_.responseText, onFailure)
  }

  def onFailure: Throwable => Throwable = _ match {
    case ex: AjaxException => AjaxClient.ErrorWithMsgException(s"${ex.xhr.statusText}. ${ex.xhr.responseText}")
    case _ => AjaxClient.UnknownErrorException
  }

  case object UnknownErrorException extends Exception("unknown error")
  case class ErrorWithMsgException(s: String) extends Exception(s)
}

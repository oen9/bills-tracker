package oen.billstracker.components

import com.payalabs.scalajs.react.bridge.{ReactBridgeComponent, WithPropsNoChildren}
import japgolly.scalajs.react.Callback

import scala.scalajs.js
import scala.scalajs.js.Date
import scala.scalajs.js.annotation.JSImport

object DatePicker extends ReactBridgeComponent {

  @JSImport("react-datepicker", JSImport.Default)
  @js.native
  object RawComponent extends js.Object

  override lazy val componentValue = RawComponent
  def apply(
    selected: js.UndefOr[Date] = js.undefined,
    onChange: js.UndefOr[Date => Callback] = js.undefined,
    dateFormat: js.UndefOr[String] = "dd.MM.yyyy"
  ): WithPropsNoChildren = autoNoChildren
}

package oen.billstracker.modules

import diode.react.ModelProxy
import oen.billstracker.components.BlueButton
import oen.billstracker.services.WebData.{Clicks, IncreaseClicks}
import oen.billstracker.shared.HelloShared
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object Home {

  case class Props(proxy: ModelProxy[Option[Clicks]])

  class Backend($: BackendScope[Props, Unit]) {
    def tick(): Callback = $.props.flatMap(_.proxy.dispatchCB(IncreaseClicks))

    def render(props: Props) =
      React.Fragment(
        <.div(^.cls := "content-head is-center",
          "Hello: " + HelloShared.TEST_STR
        ),
        <.div(^.cls := "content",
          <.div(^.cls := "l-box pure-g is-center",
            <.div(^.cls := "l-box pure-u-1 pure-u-md-1-2", BlueButton(BlueButton.Props("click me!!", tick()))),
            <.div(^.cls := "l-box pure-u-1 pure-u-md-1-2", " clicks: " + props.proxy().fold(0)(_.count))
          )
        )
      )
  }

  val component = ScalaComponent.builder[Props]("Home")
    .renderBackend[Backend]
    .build

  def apply(proxy: ModelProxy[Option[Clicks]]) = component(Props(proxy))
}

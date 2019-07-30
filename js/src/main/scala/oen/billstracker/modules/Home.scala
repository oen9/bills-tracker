package oen.billstracker.modules

import diode.react.ModelProxy
import oen.billstracker.services.WebData.{Clicks, IncreaseClicks}
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object Home {

  case class Props(proxy: ModelProxy[Option[Clicks]])

  class Backend($: BackendScope[Props, Unit]) {
    def tick(): Callback = $.props.flatMap(_.proxy.dispatchCB(IncreaseClicks))

    def render(props: Props) =
      React.Fragment(
        <.div(^.cls := "card-deck",
          <.div(^.cls := "card card-18",
            <.h5(^.cls := "card-header text-center", "June"),
            <.div(^.cls := "card-body",
              <.table(^.cls := "table table-striped table-bordered",
                <.tbody(
                  <.tr(
                    <.td(^.cls := "text-right", "items"),
                    <.td("19")
                  ),
                  <.tr(
                    <.td(^.cls := "text-right", "sum"),
                    <.td("8.99")
                  )
                )
              )
            ),
            <.div(^.cls := "card-footer text-center",
              <.button(^.cls := "btn btn-primary", "show")
            )
          ),
          <.div(^.cls := "card card-18",
            <.h5(^.cls := "card-header text-center", "May"),
            <.div(^.cls := "card-body",
              <.table(^.cls := "table table-striped table-bordered",
                <.tbody(
                  <.tr(
                    <.td(^.cls := "text-right", "items"),
                    <.td("19")
                  ),
                  <.tr(
                    <.td(^.cls := "text-right", "sum"),
                    <.td("8.99")
                  )
                )
              )
            ),
            <.div(^.cls := "card-footer text-center",
              <.button(^.cls := "btn btn-primary", "show")
            )
          )
        )
      )
  }

  val component = ScalaComponent.builder[Props]("Home")
    .renderBackend[Backend]
    .build

  def apply(proxy: ModelProxy[Option[Clicks]]) = component(Props(proxy))
}

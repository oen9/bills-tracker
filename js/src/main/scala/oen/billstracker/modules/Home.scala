package oen.billstracker.modules

import diode.react.ModelProxy
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import oen.billstracker.shared.Dto.BillGroup
import japgolly.scalajs.react.extra.router.RouterCtl
import oen.billstracker.BillsTrackerApp.Loc
import oen.billstracker.BillsTrackerApp.BillsGroupLoc
import oen.billstracker.BillsTrackerApp.NewBillsGroupLoc

object Home {

  case class Props(router: RouterCtl[Loc], proxy: ModelProxy[IndexedSeq[BillGroup]])

  class Backend($: BackendScope[Props, Unit]) {
    def render(props: Props) =
      React.Fragment(
        <.div(^.cls := "card-deck",
          props.proxy().map { group =>
            <.div(^.cls := "card card-18", ^.key := group.id.getOrElse(group.name),
              <.h5(^.cls := "card-header text-center", group.name),
              <.div(^.cls := "card-body",
                <.table(^.cls := "table table-striped table-bordered",
                  <.tbody(
                    <.tr(
                      <.td(^.cls := "text-right", "items"),
                      <.td(group.items.size)
                    ),
                    <.tr(
                      <.td(^.cls := "text-right", "sum"),
                      <.td(group.items.map(_.value).fold(BigDecimal(0))(_ + _).toString())
                    )
                  )
                )
              ),
              <.div(^.cls := "card-footer text-center",
                group.id.fold(<.button(^.cls := "btn btn-primary", ^.disabled := true, "broken group (unknown id)"): VdomElement)(id =>
                  props.router.link(BillsGroupLoc(id))(^.cls := "btn btn-primary", "show")
                )
              )
            )
          }.toVdomArray,
          if (props.proxy().isEmpty) {
            <.span(
              <.span("Don't have groups yet? Create one "),
              props.router.link(NewBillsGroupLoc)("here")
            )
          }
          else VdomArray()
        )
      )
  }

  val component = ScalaComponent.builder[Props]("Home")
    .renderBackend[Backend]
    .build

  def apply(router: RouterCtl[Loc], proxy: ModelProxy[IndexedSeq[BillGroup]]) = component(Props(router, proxy))
}

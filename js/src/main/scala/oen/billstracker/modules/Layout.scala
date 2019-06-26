package oen.billstracker.modules

import oen.billstracker.BillsTrackerApp._
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.{Resolution, RouterCtl}
import japgolly.scalajs.react.vdom.html_<^._

object Layout {

  case class MenuItem(idx: Int, label: String, location: Loc)

  case class Props(router: RouterCtl[Loc], resolution: Resolution[Loc])

  val menuItems = Seq(
    MenuItem(0, "Home", HomeLoc),
    MenuItem(1, "About", AboutLoc)
  )

  def nav(props: Props) =
    <.div(^.cls := "navbar navbar-expand-md sticky-top navbar-dark bg-dark",
      props.router.link(HomeLoc)(
        ^.cls := "navbar-brand",
        <.img(^.src := "front-res/img/logo-mini.png"),
        " bills-tracker"
      ),
      <.button(^.cls := "navbar-toggler", ^.tpe := "button", VdomAttr("data-toggle") := "collapse", VdomAttr("data-target") := "#navbarNav", ^.aria.controls := "navbarNav", ^.aria.expanded := "false", ^.aria.label := "Toggle navigation",
        <.span(^.cls := "navbar-toggler-icon")
      ),
      <.div(^.cls := "collapse navbar-collapse", ^.id := "navbarNav",
        <.ul(^.cls := "navbar-nav mr-auto",
          menuItems.map(item =>
            <.li(^.key := item.idx, ^.cls := "nav-item", (^.cls := "active").when(props.resolution.page == item.location),
              props.router.link(item.location)(^.cls := "nav-link", item.label)
            )
          ).toVdomArray
        ),
        props.router.link(SignOutLoc)(^.cls := "btn btn-secondary d-lg-inline-block", "Sign Out")
      ),
    )

  def sidebar(props: Props) =
        <.ul(^.cls := "list-group",
          props.router.link(HomeLoc)(
            ^.cls := "list-group-item list-group-item-action",
            (^.cls := "active").when(props.resolution.page == HomeLoc),
            <.i(^.cls := "fas fa-wallet"), <.span(^.cls := "pl-3", "List of bills"),

          ),
          <.li(^.cls := "list-group-item",
            <.i(^.cls := "fas fa-list-ol"), <.span(^.cls := "pl-3", "last 10 bills groups"),
            <.ul(^.cls := "list-group",
              props.router.link(BillsGroupLoc("June"))(
                ^.cls := "list-group-item list-group-item-action",
                (^.cls := "active").when(props.resolution.page == BillsGroupLoc("June")),
                <.i(^.cls := "fas fa-file-invoice-dollar"), <.span(^.cls := "pl-3", "June")
              ),
              props.router.link(BillsGroupLoc("May"))(
                ^.cls := "list-group-item list-group-item-action",
                (^.cls := "active").when(props.resolution.page == BillsGroupLoc("May")),
                <.i(^.cls := "fas fa-file-invoice-dollar"), <.span(^.cls := "pl-3", "May")
              )
            )
          ),
          props.router.link(NewBillsGroupLoc)(
            ^.cls := "list-group-item list-group-item-action",
            (^.cls := "active").when(props.resolution.page == NewBillsGroupLoc),
            <.i(^.cls := "fas fa-plus"), <.span(^.cls := "pl-3", "Add new bills group")
          ),
          props.router.link(AboutLoc)(
            ^.cls := "list-group-item list-group-item-action",
            (^.cls := "active").when(props.resolution.page == AboutLoc),
            <.i(^.cls := "fas fa-info-circle"), <.span(^.cls := "pl-3", "About")
          )
        )

  def contentBody(props: Props) = props.resolution.render()

  def footer(props: Props) =
    <.div(^.cls := "footer bg-dark text-white d-flex justify-content-center mt-auto py-3",
      "© 2019 oen"
    )

  val component = ScalaComponent.builder[Props]("Layout")
    .render_P(props => {
      props.resolution.page match {
        case _: NoLayoutLoc => props.resolution.render()
        case _ =>
          React.Fragment(
            nav(props),
            <.div(^.cls := "container-fluid p-3 mb-2 flex-shrink-0",
              <.div(^.cls := "row",
                <.div(^.cls := "col-sm col-md-2 col-xl-2", sidebar(props)),
                <.div(^.cls := "col-sm col-md-10 col-xl-10", ^.role := "main", contentBody(props))
              )
            ),
            footer(props)
          )
      }
    })
    .build

  def apply(ctl: RouterCtl[Loc], resolution: Resolution[Loc]) = component(Props(ctl, resolution))
}

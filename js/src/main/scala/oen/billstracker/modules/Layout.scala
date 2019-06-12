package oen.billstracker.modules

import oen.billstracker.BillsTrackerApp.{AboutLoc, HomeLoc, Loc, NoLayoutLoc, SignOutLoc}
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

  val component = ScalaComponent.builder[Props]("Layout")
    .render_P(props => {
      props.resolution.page match {
        case _: NoLayoutLoc => props.resolution.render()
        case _ =>
          React.Fragment(
            <.nav(^.cls := "navbar navbar-dark bg-dark navbar-expand-lg ",
              <.ul(^.cls := "navbar-nav",
                menuItems.toVdomArray(i => {
                  <.li(^.key := i.idx, ^.cls := "nav-item", (^.cls := "active").when(i.location == props.resolution.page),
                    props.router.link(i.location)(^.cls := "nav-link", i.label)
                  )
                }),
                <.li(^.cls := "nav-item", props.router.link(SignOutLoc)(^.cls := "nav-link", "Sign out")
                )
              )
            ),
            <.div(^.cls := "container",
              props.resolution.render(),
              <.div(^.cls := "footer l-box is-center",
                "footer"
              )
            )
          )
      }
    })
    .build

  def apply(ctl: RouterCtl[Loc], resolution: Resolution[Loc]) = component(Props(ctl, resolution))
}

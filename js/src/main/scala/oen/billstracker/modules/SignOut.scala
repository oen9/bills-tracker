package oen.billstracker.modules

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.extra.router.RouterCtl
import oen.billstracker.BillsTrackerApp.{Loc, HomeLoc}
import diode.react.ModelProxy
import oen.billstracker.services.WebData._

object SignOut {
  case class Props(router: RouterCtl[Loc], proxy: ModelProxy[Option[Me]])

  class Backend($: BackendScope[Props, Unit]) {

    def onMount() = for {
      p <- $.props
      _ <- p.proxy.dispatchCB(SignOutA)
    } yield ()

    def onUpdate() = for {
      p <- $.props
      _ <- if (!p.proxy().isDefined) p.router.set(HomeLoc) else Callback.empty
    } yield ()

    def render(props: Props) =
      <.div(^.cls := "login-form-bg h-100",
        <.div(^.cls := "container h-100",
          <.div(^.cls := "row justify-content-center h-100",
            <.div(^.cls := "col-xl-6",
              <.div(^.cls := "form-input-content",
                <.div(^.cls := "card login-form mb-0",
                  <.div(^.cls := "card-body pt-5",
                    props.router.link(HomeLoc)(^.cls := "text-center", <.h4("bills-tracker")),
                    <.p(^.cls := "mt-5 login-form__footer", "Signing out")
                  )
                )
              )
            )
          )
        )
      )
  }

  val component = ScalaComponent.builder[Props]("SignIn")
    .renderBackend[Backend]
    .componentDidMount(_.backend.onMount())
    .componentDidUpdate(_.backend.onUpdate())
    .build

  def apply(router: RouterCtl[Loc], proxy: ModelProxy[Option[Me]]) = component(Props(router, proxy))
}

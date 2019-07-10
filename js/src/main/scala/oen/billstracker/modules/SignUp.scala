package oen.billstracker.modules

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.extra.router.RouterCtl
import oen.billstracker.BillsTrackerApp.{Loc, HomeLoc, SignInLoc}
import diode.react.ModelProxy
import oen.billstracker.services.WebData._
import diode.react.ReactPot._
import oen.billstracker.services.WebData.TrySignUp

object SignUp {
  case class Props(router: RouterCtl[Loc], proxy: ModelProxy[SignModel])
  case class State(username: String = "",
                    password: String = "",
                    cpassword: String = "",
                    errorMsg: Option[String] = None)

  class Backend($: BackendScope[Props, State]) {
    def signIn(e: ReactEvent) = {
      e.preventDefault()
      for {
        p <- $.props
        s <- $.state
        _ <- if (s.username.isEmpty() || s.password.isEmpty() || s.cpassword.isEmpty())
              $.modState(_.copy(errorMsg = Some("username, password and confirm password required")))
            else if (s.password != s.cpassword)
              $.modState(_.copy(errorMsg = Some("invalid 'confirm password'")))
            else
              $.modState(_.copy(errorMsg = None)) >> p.proxy.dispatchCB(TrySignUp(s.username, s.password))
              // $.modState(_.copy(errorMsg = None)) >> p.proxy.dispatchCB(SignUpA(s.username, s.password))
      } yield ()
    }

    def updateUsername(e: ReactEventFromInput): Callback = {
      val newValue = e.target.value
      $.modState(_.copy(username = newValue))
    }

    def updatePassword(e: ReactEventFromInput): Callback = {
      val newValue = e.target.value
      $.modState(_.copy(password = newValue))
    }

    def updateCpassword(e: ReactEventFromInput): Callback = {
      val newValue = e.target.value
      $.modState(_.copy(cpassword = newValue))
    }

    def render(props: Props, state: State) =
      <.div(^.cls := "container mt-5",
        <.div(^.cls := "row justify-content-center",
          <.div(^.cls := "col-xl-6",
            <.div(^.cls := "card",
              <.div(^.cls := "card-header",
                props.router.link(HomeLoc)(^.cls := "text-center", <.h4("bills-tracker")),
              ),
              <.div(^.cls := "card-body",

                <.form(^.cls := "mt-4",
                  <.div(^.cls := "form-group",
                    <.input(^.tpe := "text", ^.cls := "form-control", ^.placeholder := "Username",
                    ^.value := state.username, ^.onChange ==> updateUsername)
                  ),
                  <.div(^.cls := "form-group",
                    <.input(^.tpe := "password", ^.cls := "form-control", ^.placeholder := "Password",
                    ^.value := state.password, ^.onChange ==> updatePassword)
                  ),
                  <.div(^.cls := "form-group",
                    <.input(^.tpe := "password", ^.cls := "form-control", ^.placeholder := "Confirm password",
                    ^.value := state.cpassword, ^.onChange ==> updateCpassword)
                  ),
                  <.button(^.cls := "btn btn-primary w-100", ^.onClick ==> signIn,
                    "Sign Up",
                  )
                ),

                <.p(^.cls := "mt-4", "Have account? ", props.router.link(SignInLoc)("Sign In"), " now"),
                  state.errorMsg.fold(<.div())(msg => <.div(^.cls := "alert alert-danger", msg)),
                  props.proxy().potResult.renderPending(_ =>
                    <.div(^.cls := "d-flex justify-content-center",
                      <.div(^.cls := "spinner-border text-primary", ^.role := "status",
                        <.span(^.cls := "sr-only", "Loading...")
                      )
                    )
                  ),
                  props.proxy().potResult.renderFailed(msg => <.div(^.cls := "alert alert-danger",  msg.getMessage())),
                  props.proxy().potResult.renderReady(msg => <.div(^.cls := "alert alert-success", s"$msg registered" )),
              )
            )
          )
        )
      )
  }

  val component = ScalaComponent.builder[Props]("SignUp")
    .initialState(State())
    .renderBackend[Backend]
    .componentDidMount(_.props.proxy.dispatchCB(CleanPotA))
    .build

  def apply(router: RouterCtl[Loc], proxy: ModelProxy[SignModel]) = component(Props(router, proxy))
}

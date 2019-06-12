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
      <.div(^.cls := "login-form-bg h-100",
        <.div(^.cls := "container h-100",
          <.div(^.cls := "row justify-content-center h-100",
            <.div(^.cls := "col-xl-6",
              <.div(^.cls := "form-input-content",
                <.div(^.cls := "card login-form mb-0",
                  <.div(^.cls := "card-body pt-5",
                    props.router.link(HomeLoc)(^.cls := "text-center", <.h4("bills-tracker")),

                    <.form(^.cls := "mt-5 mb-5 login-input",
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
                      <.button(^.cls := "btn login-form__btn submit w-100", ^.onClick ==> signIn,
                        "Sign Up",
                      )
                    ),

                    <.p(^.cls := "mt-5 login-form__footer", "Have account? ", props.router.link(SignInLoc)(^.cls := "text-primary", "Sign In"), " now"),
                      state.errorMsg.fold(<.div())(msg => <.div(^.cls := "alert alert-danger animated fadeInDown", msg)),
                      props.proxy().signUpResult.renderPending(_ => <.div(^.cls := "alert alert-info animated fadeInDown", "Creating account...")),
                      props.proxy().signUpResult.renderFailed(_ => <.div(^.cls := "alert alert-error animated fadeInDown", "failed")),
                      props.proxy().signUpResult.renderReady(msg => <.div(^.cls := "alert alert-success animated fadeInDown", msg)),
                  )
                )
              )
            )
          )
        )
      )
  }

  val component = ScalaComponent.builder[Props]("SignUp")
    .initialState(State())
    .renderBackend[Backend]
    .build

  def apply(router: RouterCtl[Loc], proxy: ModelProxy[SignModel]) = component(Props(router, proxy))
}

package oen.billstracker.modules

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.extra.router.RouterCtl
import oen.billstracker.BillsTrackerApp.{Loc, HomeLoc, SignUpLoc}
import diode.react.ModelProxy
import oen.billstracker.services.WebData._

object SignIn {
  case class Props(router: RouterCtl[Loc], proxy: ModelProxy[RootModel])
  case class State(username: String, password: String, errorMsg: Option[String])

  class Backend($: BackendScope[Props, State]) {
    def signIn(e: ReactEvent) = {
      e.preventDefault()
      for {
        p <- $.props
        s <- $.state
        _ <- if (s.username.isEmpty() || s.password.isEmpty()) 
              $.modState(_.copy(errorMsg = Some("username and password required")))
            else 
              $.modState(_.copy(errorMsg = None)) >> p.proxy.dispatchCB(SignInA(s.username, s.password))
      } yield ()
    }

    def onUpdate() = for {
      p <- $.props
      _ <- if (p.proxy().me.isDefined) p.router.set(HomeLoc) else Callback.empty
    } yield ()
    
    def updateUsername(e: ReactEventFromInput): Callback = {
      val newValue = e.target.value
      $.modState(_.copy(username = newValue))
    }

    def updatePassword(e: ReactEventFromInput): Callback = {
      val newValue = e.target.value
      $.modState(_.copy(password = newValue))
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
                      <.button(^.cls := "btn login-form__btn submit w-100", ^.onClick ==> signIn,
                        "Sign In",
                      )
                    ),

                    <.p(^.cls := "mt-5 login-form__footer", "Dont have account? ",
                      props.router.link(SignUpLoc)(^.cls := "text-primary", "Sign Up"),
                      " now"),
                      state.errorMsg.fold(<.div())(msg => <.div(^.cls := "alert alert-danger animated fadeInDown", msg))
                  )
                )
              )
            )
          )
        )
      )
  }

  val component = ScalaComponent.builder[Props]("SignIn")
    .initialState(State("", "", None))
    .renderBackend[Backend]
    .componentDidUpdate(_.backend.onUpdate())
    .build

  def apply(router: RouterCtl[Loc], proxy: ModelProxy[RootModel]) = component(Props(router, proxy))
}

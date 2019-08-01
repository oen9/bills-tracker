package oen.billstracker.modules

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import oen.billstracker.services.WebData.TryAddNewGroup
import diode.react.ModelProxy
import oen.billstracker.services.WebData.Me
import diode.data.Pot
import oen.billstracker.shared.Dto.BillGroup
import cats.implicits._
import diode.react.ReactPot._
import oen.billstracker.services.WebData.CleanPotA

object NewBillsGroup {

  case class Props(proxy: ModelProxy[(Option[Me], Pot[BillGroup])])
  case class State(newGroupName: String, errorMsg: Option[String] = None)

  class Backend($: BackendScope[Props, State]) {

    def addNewGroup(e: ReactEvent) = {
      e.preventDefault()

      def dispatchAddGroup(p: Props, newGroupName: String) = {
        p.proxy() match {
          case (Some(me), _) =>
            val data = TryAddNewGroup(me.token, newGroupName)
            p.proxy.dispatchCB(data)
          case _ => Callback.empty
        }
      }

      for {
        p <- $.props
        s <- $.state
        gpErrors = groupNameErrors(s.newGroupName)
        _ <- gpErrors
              .fold(dispatchAddGroup(p, s.newGroupName))(_ => $.modState(_.copy(errorMsg = gpErrors)))
      } yield ()
    }

    def groupNameErrors(groupName: String): Option[String] = {
      if (groupName.isEmpty()) "User group can't be empty".some
      else none
    }

    def updateNewGroupName(e: ReactEventFromInput): Callback = {
      val newValue = e.target.value
      val errorMsg = groupNameErrors(newValue)
      $.modState(_.copy(newGroupName = newValue, errorMsg = errorMsg))
    }

    def render(props: Props, state: State) = {
      val pot = props.proxy()._2

      <.div(^.cls := "container text-center",
        <.div(^.cls := "card",
          <.form(
            <.div(^.cls := "card-header",
              <.label(^.`for` := "group-name", "New group name"),
            ),
            <.div(^.cls := "card-body",
              <.div(^.cls := "form-group",
                <.input(^.tpe := "text", ^.cls := "form-control", ^.placeholder := "Some name",
                        ^.id := "group-name", ^.onChange ==> updateNewGroupName)
              ),
              <.button(^.cls := "btn btn-primary", "Create", ^.onClick ==> addNewGroup)
            )
          ),

          state.errorMsg.fold(<.div())(msg => <.div(^.cls := "alert alert-danger", msg)),
          pot.renderPending(_ =>
            <.div(^.cls := "d-flex justify-content-center",
              <.div(^.cls := "spinner-border text-primary", ^.role := "status",
                <.span(^.cls := "sr-only", "Loading...")
              )
            )
          ),
          pot.renderFailed(msg => <.div(^.cls := "alert alert-danger",  msg.getMessage())),
          pot.renderReady(group => <.div(^.cls := "alert alert-success", s"${group.name} created" )),
        )
      )
    }
  }

  val component = ScalaComponent.builder[Props]("NewBillsGroup")
    .initialState(State(newGroupName = ""))
    .renderBackend[Backend]
    .componentDidMount(_.props.proxy.dispatchCB(CleanPotA))
    .build

  def apply(proxy: ModelProxy[(Option[Me], Pot[BillGroup])]) = component(Props(proxy))
}

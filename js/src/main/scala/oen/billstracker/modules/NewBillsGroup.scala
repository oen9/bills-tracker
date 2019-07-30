package oen.billstracker.modules

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object NewBillsGroup {
  val component = ScalaComponent.builder[Unit]("NewBillsGroup")
    .renderStatic(
      <.div(^.cls := "container text-center",
        <.div(^.cls := "card",
          <.form(
            <.div(^.cls := "card-header",
              <.label(^.`for` := "group-name", "New group name"),
            ),
            <.div(^.cls := "card-body",
              <.div(^.cls := "form-group",
                <.input(^.tpe := "text", ^.cls := "form-control", ^.placeholder := "Some name", ^.id := "group-name")
              ),
              <.button(^.cls := "btn btn-primary", "Create")
            )
          )
        )
      )
    )
    .build

  def apply() = component()
}

package oen.billstracker.modules

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object NewBillsGroup {
  val component = ScalaComponent.builder[Unit]("NewBillsGroup")
    .renderStatic(
      <.div(^.cls := "container text-center",
        <.form(
          <.div(^.cls := "form-group",
            <.label(^.`for` := "group-name", "New group name"),
            <.input(^.tpe := "text", ^.cls := "form-control", ^.placeholder := "Some name", ^.id := "group-name")
          ),
          <.button(^.cls := "btn btn-primary", "Create")
        )
      )
    )
    .build

  def apply() = component()
}

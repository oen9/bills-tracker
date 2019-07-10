package oen.billstracker.modules

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object BillsGroup {
  val component = ScalaComponent.builder[Unit]("BillsGroup")
    .renderStatic(
      <.div(
        <.div(^.cls := "card",
          <.h5(^.cls := "card-header text-center",
            "June",
            <.button(^.cls := "btn btn-danger ml-2", "edit name"),
          ),
          <.ul(^.cls := "list-group",
            <.li(^.cls := "list-group-item",
              <.div(^.cls := "row align-items-center",
                <.div(^.cls := "col-sm col-md-5 col-xl-7", "bread"),
                <.div(^.cls := "col-sm col-md col-xl", "2.99"),
                <.div(^.cls := "col-sm col-md col-xl text-right",
                  <.div(^.cls := "btn-group", ^.role := "group",
                    <.button(^.cls := "btn btn-primary", ^.disabled := true, "accept"),
                    <.button(^.cls := "btn btn-primary", ^.disabled := true, "cancel"),
                    <.button(^.cls := "btn btn-danger", "edit")
                  )
                )
              )
            ),
            <.li(^.cls := "list-group-item",
              <.form(
                <.div(^.cls := "row align-items-center",
                  <.div(^.cls := "col-sm col-md-5 col-xl-7", <.input(^.tpe := "text", ^.cls := "form-control", ^.value := "bread")),
                  <.div(^.cls := "col-sm col-md col-xl", <.input(^.tpe := "text", ^.cls := "form-control", ^.value := "2.99")),
                  <.div(^.cls := "col-sm col-md col-xl text-right",
                    <.div(^.cls := "btn-group", ^.role := "group",
                      <.button(^.cls := "btn btn-primary", "accept"),
                      <.button(^.cls := "btn btn-primary", "cancel"),
                      <.button(^.cls := "btn btn-danger", ^.disabled := true, "edit")
                    )
                  )
                )
              ),
            ),
            <.li(^.cls := "list-group-item",
              <.div(^.cls := "row align-items-center",
                <.div(^.cls := "col-sm col-md-5 col-xl-7", "bread"),
                <.div(^.cls := "col-sm col-md-2 col-xl", "2.99"),
                <.div(^.cls := "col-sm col-md-5 col-xl text-right",
                  <.div(^.cls := "btn-group", ^.role := "group",
                    <.button(^.cls := "btn btn-primary", ^.disabled := true, "accept"),
                    <.button(^.cls := "btn btn-primary", ^.disabled := true, "cancel"),
                    <.button(^.cls := "btn btn-danger", ^.disabled := true, "edit")
                  )
                )
              )
            ),
            <.div(^.cls := "card-body text-center", <.button(^.cls := "btn btn-success", "add new position")),
            <.div(^.cls := "card-footer text-muted text-center", "8.97"),
          ),
        )
      )
    )
    .build

  def apply() = component()
}

package oen.billstracker.modules

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

object About {
  val component = ScalaComponent.builder[Unit]("About")
    .renderStatic(
      <.div(^.cls := "container",
        <.div(^.cls := "card",
          <.div(^.cls := "card-header", "About"),
          <.div(^.cls := "card-body",
            <.table(^.cls := "table table-striped",
              <.tbody(
                <.tr(
                  <.td("author"),
                  <.td("oen")
                ),
                <.tr(
                  <.td("github"),
                  <.td(<.a(^.target := "_blank", ^.href := "https://github.com/oen9/bills-tracker", "https://github.com/oen9/bills-tracker"))
                ),
                <.tr(
                  <.td("heroku"),
                  <.td(<.a(^.target := "_blank", ^.href := "https://bills-tracker.herokuapp.com", "https://bills-tracker.herokuapp.com"))
                ),
                <.tr(
                  <.td("use"),
                  <.td("do whatever you want!")
                )
              )
            )
          )
        )
      )
    )
    .build

  def apply() = component()
}

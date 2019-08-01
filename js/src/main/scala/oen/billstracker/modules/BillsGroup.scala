package oen.billstracker.modules

import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import diode.react.ModelProxy
import oen.billstracker.shared.Dto.BillGroup
import oen.billstracker.shared.Dto.BillItem
import cats.implicits._
import com.softwaremill.quicklens._
import io.scalaland.chimney.dsl._
import scala.util.Try

object BillsGroup {
  case class Props(proxy: ModelProxy[Option[BillGroup]])
  case class State(toDelete: Option[BillItem] = None, toEdit: Option[EditedBillItem] = None)
  case class EditedBillItem(id: Option[String], description: String, value: String)

  class Backend($: BackendScope[Props, State]) {
    def pickToDelete(toDelete: BillItem)(e: ReactEvent) = {
      e.preventDefault()
      $.modState(_.copy(toDelete = toDelete.some))
    }

    def deleteItem(e: ReactEvent) = for {
      _ <- e.preventDefaultCB
      _ <- Callback(println("deleteItem"))
      s <- $.state
      _ <- Callback(println(s"Fake deleting ${s.toDelete}"))
      _ <- $.modState(_.copy(toDelete = None))
    } yield ()

    def clearToDelete(e: ReactEvent) = for {
      _ <- e.preventDefaultCB
      _ <- Callback(println("clearToDelete"))
      _ <- $.modState(_.copy(toDelete = None))
    } yield ()

    def pickToEdit(toEdit: BillItem)(e: ReactEvent) = for {
      _ <- e.preventDefaultCB
      _ <- Callback(println("pickToEdit"))
      _ <- $.modState(_.copy(toEdit = toEdit.into[EditedBillItem]
        .withFieldComputed(_.value, _.value.toString())
        .transform
        .some
      ))
    } yield ()

    def acceptEdit(e: ReactEvent) = for {
      _ <- e.preventDefaultCB
      _ <- Callback(println("acceptEdit"))
      s <- $.state
      asBillItem = s.toEdit.map(_.into[BillItem]
        .withFieldComputed(_.value, edited => parseBigDecimal(edited.value))
        .transform
      )
      _ <- Callback(println(s"$asBillItem has been edited"))
      _ <- $.modState(_.copy(toEdit = None))
    } yield ()

    def clearToEdit(e: ReactEvent) = for {
      _ <- e.preventDefaultCB
      _ <- Callback(println("reverting edit.."))
      _ <- $.modState(_.copy(toEdit = None))
    } yield ()

    def updateDescription(e: ReactEventFromInput): Callback = {
      e.preventDefault()
      val newValue = e.target.value
      $.modState(_.modify(_.toEdit.each.description).setTo(newValue)) >> e.preventDefaultCB >> Callback(println("updateDescription"))
    }

    def updateValue(e: ReactEventFromInput): Callback = {
      e.preventDefault()
      val newValue = e.target.value
      Try(parseBigDecimal(newValue)).fold(
        _ => Callback.empty,
        _ => $.modState(_.modify(_.toEdit.each.value).setTo(newValue))
      ) >> e.preventDefaultCB >> Callback(println("updateValue"))
    }

    def parseBigDecimal(s: String): BigDecimal = {
      if (s.isEmpty) BigDecimal(0)
      else BigDecimal(s)
    }

    def addNewBillItem(e: ReactEvent) = for {
      _ <- e.preventDefaultCB
      _ <- Callback(println("Fake addNewBillItem"))
    } yield ()

    def pickNameToEdit(e: ReactEvent) = for {
      _ <- e.preventDefaultCB
      _ <- Callback(println("Fake pickNameToEdit"))
    } yield ()

    def pickNameToDelete(e: ReactEvent) = for {
      _ <- e.preventDefaultCB
      _ <- Callback(println("Fake pickNameToDelete"))
    } yield ()

    def render(props: Props, state: State) = {
      val toEdit = state.toEdit
      <.div(
        <.div(^.cls :="modal", ^.id := "exampleModal", ^.tabIndex := -1, ^.role := "dialog",
          <.div(^.cls :="modal-dialog modal-dialog-centered", ^.role := "document",
            <.div(^.cls := "modal-content",
              <.div(^.cls := "modal-header",
                <.h5(^.cls := "modal-title", "Confirm"),
                <.button(^.tpe := "button", ^.cls := "close", VdomAttr("data-dismiss") := "modal", ^.aria.label := "Close",
                  <.span(^.aria.hidden := "true", "×"),
                ),
              ),
              <.div(^.cls :="modal-body",
                state.toDelete.fold(<.div("Error: there is no item to delete"))(item =>
                  <.div(
                    <.p("Are you sure you want to delete this item?"),
                    <.p(s"${item.description}"),
                    <.p(s"${item.value}")
                  )
                ),
              ),
              <.div(^.cls :="modal-footer",
                <.button(^.tpe := "button", ^.cls :="btn btn-secondary", VdomAttr("data-dismiss") := "modal", "Close", ^.onClick ==> clearToDelete),
                <.button(^.tpe := "button", ^.cls :="btn btn-primary", VdomAttr("data-dismiss") := "modal", "Delete item", ^.onClick ==> deleteItem)
              )
            )
          )
        ),

        <.form(
          <.div(^.cls := "card",
            <.h5(^.cls := "card-header text-center",
              "June",
              <.div(^.cls := "btn-group float-right", ^.role := "group",
                <.button(^.tpe := "button", ^.cls := "btn btn-warning", <.i(^.cls := "fas fa-edit"), ^.onClick ==> pickNameToEdit),
                <.button(^.tpe := "button", ^.cls := "btn btn-danger", <.i(^.cls := "fas fa-trash"), ^.onClick ==> pickNameToDelete)
              )
            ),
            <.ul(^.cls := "list-group",
              props.proxy().fold(<.div("wrong group id"): VdomNode)(group =>
                React.Fragment(
                  group.items.map { item =>
                    <.li(^.cls := "list-group-item", ^.key := item.id.getOrElse(item.description),
                      <.div(^.cls := "row align-items-center",
                        <.div(^.cls := "col-sm col-md-5 col-xl-7",
                          toEdit.filter(_.id == item.id).fold(item.description.toString(): VdomNode)(e =>
                            <.input(^.tpe := "text", ^.cls := "form-control", ^.value := e.description, ^.onChange ==> updateDescription)
                          )
                        ),
                        <.div(^.cls := "col-sm col-md col-xl",
                          toEdit.filter(_.id == item.id).fold(item.value.toString(): VdomNode)(e =>
                            <.input(^.tpe := "text", ^.cls := "form-control", ^.value := e.value.toString, ^.onChange ==> updateValue)
                          )
                        ),
                        <.div(^.cls := "col-sm col-md col-xl text-right",
                          toEdit match {
                            case None =>
                              <.div(^.cls := "btn-group", ^.role := "group",
                                <.button(^.cls := "btn btn-warning", <.i(^.cls := "fas fa-edit"), ^.onClick ==> pickToEdit(item)),
                                <.button(^.cls := "btn btn-danger", <.i(^.cls := "fas fa-trash"), ^.onClick ==> pickToDelete(item),
                                  VdomAttr("data-toggle") :="modal", VdomAttr("data-target") := "#exampleModal"),
                              )
                            case Some(e) if e.id == item.id =>
                              <.div(^.cls := "btn-group", ^.role := "group",
                                <.button(^.cls := "btn btn-success", <.i(^.cls := "fas fa-check"), ^.onClick ==> acceptEdit),
                                <.button(^.cls := "btn btn-primary", <.i(^.cls := "fas fa-history"), ^.onClick ==> clearToEdit)
                              )
                            case Some(_) =>
                              <.div(^.cls := "btn-group", ^.role := "group",
                                <.button(^.cls := "btn btn-warning", <.i(^.cls := "fas fa-edit"), ^.disabled := true),
                                <.button(^.cls := "btn btn-danger", <.i(^.cls := "fas fa-trash"), ^.disabled := true)
                              )
                          }
                        )
                      )
                    )
                  }.toVdomArray,
                  <.div(^.cls := "card-body text-center",
                    <.button(^.cls := "btn btn-success", <.i(^.cls := "fas fa-plus"), ^.onClick ==> addNewBillItem)
                  ),
                  <.div(^.cls := "card-footer text-muted text-center", group.items.map(_.value).sum.toString)
                )
              )
            )
          )
        )
      )
    }
  }

  val component = ScalaComponent.builder[Props]("BillsGroup")
    .initialState(State())
    .renderBackend[Backend]
    .build

  def apply(proxy: ModelProxy[Option[BillGroup]]) = component(Props(proxy))
}

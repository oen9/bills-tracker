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
import oen.billstracker.services.WebData._

object BillsGroup {
  case class Props(proxy: ModelProxy[(Option[Me], Option[BillGroup])])
  case class State(
    toDelete: Option[BillItem] = None,
    toEdit: Option[EditedBillItem] = None,
    groupToDelete: Option[BillGroup] = None,
    groupNameToEdit: Option[String] = None
  )
  case class EditedBillItem(id: Option[String], description: String, value: String)

  class Backend($: BackendScope[Props, State]) {
    def pickToDelete(toDelete: BillItem)(e: ReactEvent) = {
      e.preventDefault()
      $.modState(_.copy(toDelete = toDelete.some))
    }

    def deleteItem(e: ReactEvent) = for {
      _ <- e.preventDefaultCB
      s <- $.state
      p <- $.props
      deleteItemAction = for {
          me <- p.proxy()._1
          group <- p.proxy()._2
          groupId <- group.id
          item <- s.toDelete
          itemId <- item.id
        } yield DeleteItemA(me.token, groupId, itemId)
      _ <- deleteItemAction.fold(Callback.empty)(p.proxy.dispatchCB)
      _ <- $.modState(_.copy(toDelete = None))
    } yield ()

    def clearToDelete(e: ReactEvent) = for {
      _ <- e.preventDefaultCB
      _ <- $.modState(_.copy(toDelete = None))
    } yield ()

    def pickToEdit(toEdit: BillItem)(e: ReactEvent) = for {
      _ <- e.preventDefaultCB
      _ <- $.modState(_.copy(toEdit = toEdit.into[EditedBillItem]
        .withFieldComputed(_.value, _.value.toString())
        .transform
        .some
      ))
    } yield ()

    def acceptEdit(e: ReactEvent) = for {
      _ <- e.preventDefaultCB
      s <- $.state
      p <- $.props
      maybeBillItem = s.toEdit.map(_.into[BillItem]
          .withFieldComputed(_.value, edited => parseBigDecimal(edited.value))
          .transform
        )
      updateBillItemAction = for {
          me <- p.proxy()._1
          group <- p.proxy()._2
          groupId <- group.id
          billItem <- maybeBillItem
          itemId <- billItem.id
        } yield UpdateItemA(me.token, groupId, itemId, billItem)
      _ <- updateBillItemAction.fold(Callback.empty)(p.proxy.dispatchCB)
      _ <- $.modState(_.copy(toEdit = None))
    } yield ()

    def clearToEdit(e: ReactEvent) = for {
      _ <- e.preventDefaultCB
      _ <- Callback(println("reverting edit.."))
      _ <- $.modState(_.copy(toEdit = None))
    } yield ()

    def updateDescription(e: ReactEventFromInput): Callback = for {
      _ <- e.preventDefaultCB
      newValue = e.target.value
      _ <- $.modState(_.modify(_.toEdit.each.description).setTo(newValue))
    } yield ()

    def updateValue(e: ReactEventFromInput): Callback = for {
      _ <- e.preventDefaultCB
      newValue = e.target.value
      _ <- Try(parseBigDecimal(newValue)).fold(
        _ => Callback.empty,
        _ => $.modState(_.modify(_.toEdit.each.value).setTo(newValue))
      )
    } yield ()

    def parseBigDecimal(s: String): BigDecimal = {
      if (s.isEmpty) BigDecimal(0)
      else BigDecimal(s)
    }

    def addNewBillItem(e: ReactEvent) = for {
      _ <- e.preventDefaultCB
      p <- $.props
      addNewItemAction = for {
          me <- p.proxy()._1
          group <- p.proxy()._2
          groupId <- group.id
        } yield AddNewItemA(me.token, groupId)
      _ <- addNewItemAction.fold(Callback.empty)(p.proxy.dispatchCB)
    } yield ()

    def pickGroupNameToEdit(groupName: String)(e: ReactEvent) = for {
      _ <- e.preventDefaultCB
      _ <- $.modState(_.copy(groupNameToEdit = groupName.some))
    } yield ()

    def updateGroupName(e: ReactEventFromInput): Callback = for {
      _ <- e.preventDefaultCB
      _ <- e.stopPropagationCB
      newValue = e.target.value
      _ <- $.modState(_.modify(_.groupNameToEdit.each).setTo(newValue))
    } yield ()

    def acceptEditGroupName(e: ReactEvent) = for {
      _ <- e.preventDefaultCB
      s <- $.state
      p <- $.props
      updateGroupNameAction = for {
        me <- p.proxy()._1
        group <- p.proxy()._2
        groupId <- group.id
        groupNameToEdit <- s.groupNameToEdit
      } yield UpdateGroupNameA(me.token, groupId, groupNameToEdit)
      _ <- updateGroupNameAction.fold(Callback.empty)(p.proxy.dispatchCB)
      _ <- $.modState(_.copy(groupNameToEdit = None))
    } yield ()

    def clearGroupName(e: ReactEvent) = for {
      _ <- e.preventDefaultCB
      _ <- Callback(println("clear group name to edit"))
      _ <- $.modState(_.copy(groupNameToEdit = None))
    } yield ()

    def pickGroupToDelete(group: BillGroup)(e: ReactEvent) = for {
      _ <- e.preventDefaultCB
      _ <- $.modState(_.copy(groupToDelete = group.some))
      _ <- Callback(println("Fake pickGroupToDelete"))
    } yield ()

    def deleteGroup(e: ReactEvent) = for {
      _ <- e.preventDefaultCB
      _ <- Callback(println("delete GROUP"))
      s <- $.state
      _ <- Callback(println(s"Fake deleting ${s.groupToDelete}"))
      _ <- $.modState(_.copy(groupToDelete = None))
    } yield ()

    def clearGroupToDelete(e: ReactEvent) = for {
      _ <- e.preventDefaultCB
      _ <- Callback(println("clear GROUP to delete"))
      _ <- $.modState(_.copy(groupToDelete = None))
    } yield ()

    def render(props: Props, state: State) = {
      val toEdit = state.toEdit

      def deleteItemModal() =
        <.div(^.cls :="modal", ^.id := "deleteItemModal", ^.tabIndex := -1, ^.role := "dialog",
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
                    <.p(s"description: ${item.description}"),
                    <.p(s"value: ${item.value}")
                  )
                ),
              ),
              <.div(^.cls :="modal-footer",
                <.button(^.tpe := "button", ^.cls :="btn btn-secondary", VdomAttr("data-dismiss") := "modal", "Close", ^.onClick ==> clearToDelete),
                <.button(^.tpe := "button", ^.cls :="btn btn-danger", VdomAttr("data-dismiss") := "modal", "Delete item", ^.onClick ==> deleteItem)
              )
            )
          )
        )

      def deleteGroupModal() =
        <.div(^.cls :="modal", ^.id := "deleteGroupModal", ^.tabIndex := -1, ^.role := "dialog",
          <.div(^.cls :="modal-dialog modal-dialog-centered", ^.role := "document",
            <.div(^.cls := "modal-content",
              <.div(^.cls := "modal-header",
                <.h5(^.cls := "modal-title", "Confirm"),
                <.button(^.tpe := "button", ^.cls := "close", VdomAttr("data-dismiss") := "modal", ^.aria.label := "Close",
                  <.span(^.aria.hidden := "true", "×"),
                ),
              ),
              <.div(^.cls :="modal-body",
                state.groupToDelete.fold(<.div("Error: there is no group to delete"))(group =>
                  <.div(
                    <.p("Are you sure you want to delete this Group?"),
                    <.p(s"name: ${group.name}"),
                    <.p(s"items: ${group.items.size}"),
                    <.p(s"value: ${group.items.map(_.value).sum.toString}")
                  )
                ),
              ),
              <.div(^.cls :="modal-footer",
                <.button(^.tpe := "button", ^.cls :="btn btn-secondary", VdomAttr("data-dismiss") := "modal", "Close", ^.onClick ==> clearGroupToDelete),
                <.button(^.tpe := "button", ^.cls :="btn btn-danger", VdomAttr("data-dismiss") := "modal", "Delete item", ^.onClick ==> deleteGroup)
              )
            )
          )
        )

      def itemList(items: IndexedSeq[BillItem]) = items.map { item =>
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
              <.div(^.cls := "btn-group", ^.role := "group",
                toEdit match {
                  case None =>
                    React.Fragment(
                      <.button(^.tpe := "button", ^.cls := "btn btn-warning", <.i(^.cls := "fas fa-edit"), ^.onClick ==> pickToEdit(item)),
                      <.button(^.tpe := "button", ^.cls := "btn btn-danger", <.i(^.cls := "fas fa-trash"), ^.onClick ==> pickToDelete(item),
                        VdomAttr("data-toggle") :="modal", VdomAttr("data-target") := "#deleteItemModal"),
                    )
                  case Some(e) if e.id == item.id =>
                    React.Fragment(
                      <.button(^.tpe := "button", ^.cls := "btn btn-success", <.i(^.cls := "fas fa-check"), ^.onClick ==> acceptEdit),
                      <.button(^.tpe := "button", ^.cls := "btn btn-primary", <.i(^.cls := "fas fa-history"), ^.onClick ==> clearToEdit)
                    )
                  case Some(_) =>
                    React.Fragment(
                      <.button(^.tpe := "button", ^.cls := "btn btn-warning", <.i(^.cls := "fas fa-edit"), ^.disabled := true),
                      <.button(^.tpe := "button", ^.cls := "btn btn-danger", <.i(^.cls := "fas fa-trash"), ^.disabled := true)
                    )
                }
              )
            )
          )
        )
      }.toVdomArray

      def groupNameHeader(group: BillGroup) =
        <.h5(^.cls := "card-header text-center",
          <.div(^.cls := "row align-items-center",

            <.div(^.cls := "col-sm col-md-9 col-xl-10",
              state.groupNameToEdit.fold(group.name: VdomNode)(groupNameToEdit =>
                <.input(^.tpe := "text", ^.cls := "form-control", ^.value := groupNameToEdit, ^.onChange ==> updateGroupName)
              )
            ),
            <.div(^.cls := "col-sm col-md col-xl text-right",
              <.div(^.cls := "btn-group", ^.role := "group",
                state.groupNameToEdit.fold(
                  React.Fragment(
                    <.button(^.tpe := "button", ^.cls := "btn btn-warning", <.i(^.cls := "fas fa-edit"), ^.onClick ==> pickGroupNameToEdit(group.name)),
                    <.button(^.tpe := "button", ^.cls := "btn btn-danger", <.i(^.cls := "fas fa-trash"), ^.onClick ==> pickGroupToDelete(group),
                      VdomAttr("data-toggle") :="modal", VdomAttr("data-target") := "#deleteGroupModal")
                  )
                )(groupNameToEdit =>
                  React.Fragment(
                    <.button(^.tpe := "button", ^.cls := "btn btn-success", <.i(^.cls := "fas fa-check"), ^.onClick ==> acceptEditGroupName),
                    <.button(^.tpe := "button", ^.cls := "btn btn-primary", <.i(^.cls := "fas fa-history"), ^.onClick ==> clearGroupName)
                  )
                )
              )
            )
          )
        )

      <.div(

        deleteItemModal(),
        deleteGroupModal(),

        <.form(^.action := "javascript:void(0);",
          <.div(^.cls := "card",
            props.proxy()._2.fold(<.div("This group doesn't exist anymore."): VdomNode)(group =>
              React.Fragment(
                groupNameHeader(group),
                <.ul(^.cls := "list-group",
                  itemList(group.items),
                  <.div(^.cls := "card-body text-center",
                      <.button(^.tpe := "button", ^.cls := "btn btn-success", <.i(^.cls := "fas fa-plus"), ^.onClick ==> addNewBillItem)
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

  def apply(proxy: ModelProxy[(Option[Me], Option[BillGroup])]) = component(Props(proxy))
}

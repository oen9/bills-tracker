package oen.billstracker.services

import diode.{ActionHandler, Circuit, ModelRW}
import diode.react.ReactConnector
import WebData._
import handlers._
import com.softwaremill.quicklens._

class ClicksHandler[M](modelRW: ModelRW[M, Option[Clicks]]) extends ActionHandler(modelRW) {
  override def handle = {
    case IncreaseClicks =>
      val newValue = value.fold(Option.empty[Clicks])(c => Some(c.copy(c.count + 1))) // TODO cats
      updated(newValue)
  }
}

object AppCircuit extends Circuit[RootModel] with ReactConnector[RootModel] {
  override protected def initialModel: RootModel = RootModel()

  override protected def actionHandler: AppCircuit.HandlerFunction = composeHandlers(
    new ClicksHandler(zoomMapRW(_.me)(_.clicks)((root, optClick) => { // TODO remove
      val updMe = for {
        me <- root.me
        clicks <- optClick
      } yield me.copy(clicks = clicks)
      root.copy(me = updMe)
    })),
    new MeSignHandler(zoomTo(_.me)),
    new UserHandler(zoomTo(_.user)),
    new GroupsHandler(zoomToGroups),
    new ItemsHandler(zoomToGroups),
    foldHandlers( // to broadcast actions e.g. ClearPotA
      new SignHandler(zoomTo(_.signModel.potResult)),
      new NewGroupHandler(zoomTo(_.pots.newGroupResult))
    )
  )

  private[this] def zoomToGroups =
    zoomMapRW(_.user)(_.billsGroups)((root, maybeGroups) => {
      maybeGroups.fold(root)(newGroups =>
        root.modify(_.user.each.billsGroups).setTo(newGroups)
      )
    })
}

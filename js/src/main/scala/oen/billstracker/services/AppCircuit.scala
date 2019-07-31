package oen.billstracker.services

import diode.{ActionHandler, Circuit, ModelRW}
import diode.react.ReactConnector
import WebData._
import handlers._

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
    new ClicksHandler(zoomMapRW(_.me)(_.clicks)((root, optClick) => { // TODO lenses?
      val updMe = for {
        me <- root.me
        clicks <- optClick
      } yield me.copy(clicks = clicks)
      root.copy(me = updMe)
    })),
    new GenericSignHandler(zoomTo(_.me)),
    new SignHandler(zoomTo(_.signModel.potResult)),
    new UserHandler(zoomTo(_.user))
  )
}

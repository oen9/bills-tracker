package oen.billstracker

import oen.billstracker.modules.{About, Home, Layout}
import oen.billstracker.services.AppCircuit
import japgolly.scalajs.react.extra.router._
import org.scalajs.dom.html

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import japgolly.scalajs.react.CallbackTo
import oen.billstracker.modules.SignIn
import oen.billstracker.modules.SignOut
import oen.billstracker.modules.SignUp
import oen.billstracker.modules.BillsGroup
import oen.billstracker.modules.NewBillsGroup
import oen.billstracker.shared.Dto.User

@JSExportTopLevel("BillsTrackerApp")
object BillsTrackerApp {

  sealed abstract class Loc(val name: String)
  abstract class NoLayoutLoc(name: String) extends Loc(name)

  case object HomeLoc extends Loc("Home")
  case object AboutLoc extends Loc("About")
  case object NewBillsGroupLoc extends Loc("New bills group")
  case object SignInLoc extends NoLayoutLoc("Sign in")
  case object SignUpLoc extends NoLayoutLoc("Sign up")
  case object SignOutLoc extends NoLayoutLoc("Sign out")
  case class BillsGroupLoc(id: String) extends Loc("Bills group")

  @JSExport
  def main(target: html.Div): Unit = {

    val emptyUser = User(name = "unknown")
    val meWrapper = AppCircuit.connect(_.me)
    val signWrapper = AppCircuit.connect(_.signModel)
    val homeWrapper = AppCircuit.connect(_.me.map(_.clicks))
    val rootWrapper = AppCircuit.connect(identity(_))
    val userWrapper = AppCircuit.connect(_.user.fold(emptyUser)(identity))

    val routerConfig = RouterConfigDsl[Loc].buildConfig { dsl =>
      import dsl._

      def grantPrivateAccess: CallbackTo[Boolean] = CallbackTo {
        AppCircuit.zoom(_.me).value.isDefined
      }

      val restrictedRoutes = (emptyRule
        | staticRoute(root, HomeLoc) ~> render(homeWrapper(Home(_)))
        | dynamicRouteCT("#bills-group" / remainingPath.caseClass[BillsGroupLoc]) ~> dynRender(_ => BillsGroup())
        | staticRoute("#about", AboutLoc) ~> render(About())
        | staticRoute("#new-bills-group", NewBillsGroupLoc) ~> render(NewBillsGroup())
        ).addCondition(grantPrivateAccess)(_ => redirectToPage(SignInLoc)(Redirect.Push))

      val freeRoutes = (emptyRule
        | staticRoute("#sign-out", SignOutLoc) ~> renderR(router => meWrapper(SignOut(router, _)))
        | staticRoute("#sign-in", SignInLoc) ~> renderR(router => rootWrapper(SignIn(router, _)))
        | staticRoute("#sign-up", SignUpLoc) ~> renderR(router => signWrapper(SignUp(router, _)))
        )

      (restrictedRoutes | freeRoutes)
        .notFound(redirectToPage(HomeLoc)(Redirect.Replace))
        .setTitle(p => s"bills-tracker | ${p.name}")
    }.renderWith((ctl, resolution) => userWrapper(Layout(ctl, resolution, _)))

    val router = Router(BaseUrl.until_#, routerConfig)
    router().renderIntoDOM(target)
  }
}

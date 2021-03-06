package oen.billstracker

import oen.billstracker.modules.{About, Home, Layout}
import oen.billstracker.services.AppCircuit
import japgolly.scalajs.react.extra.router._
import org.scalajs.dom.document

import japgolly.scalajs.react.CallbackTo
import oen.billstracker.modules.SignIn
import oen.billstracker.modules.SignOut
import oen.billstracker.modules.SignUp
import oen.billstracker.modules.BillsGroup
import oen.billstracker.modules.NewBillsGroup
import oen.billstracker.shared.Dto.User
import cats.implicits._
import scala.scalajs.js.annotation.JSImport

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

  import scala.scalajs.js
  @JSImport("bootstrap", JSImport.Default)
  @js.native
  object Bootstrap extends js.Object

  def main(args: Array[String]): Unit = {
    val target = document.getElementById("main")

    Bootstrap

    val emptyUser = User(name = "unknown")
    val meWrapper = AppCircuit.connect(_.me)
    val signWrapper = AppCircuit.connect(_.signModel)
    val homeWrapper = AppCircuit.connect(_.user.map(_.billsGroups).getOrElse(IndexedSeq()))
    val rootWrapper = AppCircuit.connect(identity(_))
    val userWrapper = AppCircuit.connect(_.user.fold(emptyUser)(identity))
    val newGroupWrapper = AppCircuit.connect {
      val meModel = AppCircuit.zoom(_.me)
      val newGroupModel = AppCircuit.zoom(_.pots.newGroupResult)
      meModel.zip(newGroupModel)
    }
    def billGroupWrapper(id: String) = AppCircuit.connect {
      val meModel = AppCircuit.zoom(_.me)
      val groupsModel = AppCircuit.zoom(_.user.flatMap(_.billsGroups.filter(_.id == id.some).headOption))
      meModel.zip(groupsModel)
    }

    val routerConfig = RouterConfigDsl[Loc].buildConfig { dsl =>
      import dsl._

      def grantPrivateAccess: CallbackTo[Boolean] = CallbackTo {
        AppCircuit.zoom(_.me).value.isDefined
      }

      val restrictedRoutes = (emptyRule
        | staticRoute(root, HomeLoc) ~> renderR(router => homeWrapper(Home(router, _)))
        | dynamicRouteCT("#bills-group" / remainingPath.caseClass[BillsGroupLoc]) ~> dynRender(loc => billGroupWrapper(loc.id)(BillsGroup(_)))
        | staticRoute("#about", AboutLoc) ~> render(About())
        | staticRoute("#new-bills-group", NewBillsGroupLoc) ~> render(newGroupWrapper(NewBillsGroup(_)))
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

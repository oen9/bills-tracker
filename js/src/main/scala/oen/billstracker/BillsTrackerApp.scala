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

@JSExportTopLevel("BillsTrackerApp")
object BillsTrackerApp {

  sealed abstract class Loc(val name: String)
  abstract class NoLayoutLoc(name: String) extends Loc(name)

  case object HomeLoc extends Loc("Home")
  case object AboutLoc extends Loc("About")
  case object SignInLoc extends NoLayoutLoc("Sign in")
  case object SignUpLoc extends NoLayoutLoc("Sign up")
  case object SignOutLoc extends NoLayoutLoc("Sign out")

  @JSExport
  def main(target: html.Div): Unit = {

    val meWrapper = AppCircuit.connect(_.me)
    val signWrapper = AppCircuit.connect(_.signModel)
    val homeWrapper = AppCircuit.connect(_.me.map(_.clicks))
    val rootWrapper = AppCircuit.connect(identity(_))

    val routerConfig = RouterConfigDsl[Loc].buildConfig { dsl =>
      import dsl._

      def grantPrivateAccess: CallbackTo[Boolean] = CallbackTo {
        AppCircuit.zoom(_.me).value.isDefined
      }

      val restrictedRoutes = (emptyRule
        | staticRoute(root, HomeLoc) ~> render(homeWrapper(Home(_)))
        | staticRoute("#about", AboutLoc) ~> render(About())
        ).addCondition(grantPrivateAccess)(_ => redirectToPage(SignInLoc)(Redirect.Push))

      val freeRoutes = (emptyRule
        | staticRoute("#signOut", SignOutLoc) ~> renderR(router => meWrapper(SignOut(router, _)))
        | staticRoute("#signIn", SignInLoc) ~> renderR(router => rootWrapper(SignIn(router, _)))
        | staticRoute("#signUp", SignUpLoc) ~> renderR(router => signWrapper(SignUp(router, _)))
        )

      (restrictedRoutes | freeRoutes)
        .notFound(redirectToPage(HomeLoc)(Redirect.Replace))
        .setTitle(p => s"bills-tracker | ${p.name}")
    }.renderWith(Layout.apply)

    val router = Router(BaseUrl.until_#, routerConfig)
    router().renderIntoDOM(target)
  }
}

package oen.billstracker

import java.util.concurrent.Executors

import cats.effect._
import cats.implicits._
import oen.billstracker.config.AppConfig
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.implicits._

import scala.concurrent.ExecutionContext
import oen.billstracker.endpoints.AuthEndpoints
import oen.billstracker.services.AuthService
import oen.billstracker.endpoints.WelcomeEndpoints
import oen.billstracker.endpoints.UserEndpoints
import oen.billstracker.services.MongoService
import org.http4s.server.Router
import oen.billstracker.endpoints.GroupsEndpoints
import oen.billstracker.services.GroupsService
import org.http4s.server.middleware.CORSConfig
import scala.concurrent.duration._
import org.http4s.server.middleware.CORS

object App extends IOApp {

  val originConfig = CORSConfig(
    anyOrigin = true,
    allowCredentials = false,
    maxAge = 1.day.toSeconds)

  override def run(args: List[String]): IO[ExitCode] = {
    (for {
      blockingEc <- createEc[IO](4)
      dbEc <- createEc[IO](4)
    } yield (blockingEc, dbEc)).use { case (blockingEc, dbEc) =>
      createServer[IO](blockingEc, dbEc)
    }
  }

  def createServer[F[_] : ContextShift : ConcurrentEffect : Timer](
    blockingEc: ExecutionContext,
    dbEc: ExecutionContext): F[ExitCode] = {

    val closableServices = for {
      conf <- Resource.liftF(AppConfig.read())
      mongoService <- MongoService[F](conf.mongo.uri)(Effect[F], dbEc)
    } yield (conf, mongoService)

    closableServices.use{ case (conf, mongoService) =>
      for {
        conf <- AppConfig.read()
        blockingEc = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(4))

        authService <- AuthService[F](conf.secret, mongoService)
        groupsService = GroupsService[F](mongoService)

        staticEndpoints = StaticEndpoints[F](conf.assets, blockingEc)
        authEndpoints = AuthEndpoints[F](authService)
        welcomeEndpoints = WelcomeEndpoints[F](authEndpoints.authMiddleware)
        userEndpoints = UserEndpoints[F](authEndpoints.authMiddleware)
        groupsEndpoints = GroupsEndpoints[F](authEndpoints.authMiddleware, groupsService)

        httpApp =
          (
            staticEndpoints.endpoints
            <+> authEndpoints.endpoints
            <+> welcomeEndpoints.endpoints
            <+> userEndpoints.endpoints
            <+> Router("/groups" -> groupsEndpoints.endpoints)
          ).orNotFound

        exitCode <- BlazeServerBuilder[F]
          .bindHttp(conf.http.port, conf.http.host)
          .withHttpApp(CORS(httpApp))
          .serve
          .compile
          .drain
          .as(ExitCode.Success)
      } yield exitCode
    }
  }

  def createEc[F[_] : Effect](nThreads: Int): Resource[F, ExecutionContext] =
    Resource[F, ExecutionContext](
      Effect[F].delay {
        val executor = Executors.newFixedThreadPool(nThreads)
        val ec = ExecutionContext.fromExecutor(executor)
        (ec, Effect[F].delay(executor.shutdown()))
      }
    )
}

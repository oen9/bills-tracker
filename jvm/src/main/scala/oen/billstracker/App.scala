package oen.billstracker

import java.util.concurrent.Executors

import cats.effect._
import cats.implicits._
import oen.billstracker.config.AppConfig
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.implicits._

import scala.concurrent.ExecutionContext

object App extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    createServer[IO]()
  }

  def createServer[F[_] : ContextShift : ConcurrentEffect : Timer](): F[ExitCode] = {
    for {
      conf <- AppConfig.read()
      blockingEc = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(4))
      staticEndpoints = StaticEndpoints[F](blockingEc)
      httpApp = staticEndpoints.endpoints().orNotFound
      exitCode <- BlazeServerBuilder[F]
        .bindHttp(conf.http.port, conf.http.host)
        .withHttpApp(httpApp)
        .serve
        .compile
        .drain
        .as(ExitCode.Success)
    } yield exitCode
  }
}
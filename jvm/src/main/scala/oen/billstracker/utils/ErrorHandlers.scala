package oen.billstracker.utils

import cats.implicits._
import cats.effect.Effect

object ErrorHandlers {

  implicit class handleErrOps[F[_] : Effect, A](effect: F[A]) {
    def handleErr(implicit logger: org.log4s.Logger): F[Option[A]] = handleErr("Error occurred.")

    def handleErr(msg: String)(implicit logger: org.log4s.Logger): F[Option[A]] = {
      def handler(t: Throwable) = Effect[F].delay {
        logger.error(t)(msg)
        Option.empty[A]
      }

      effect
        .map(_.some)
        .handleErrorWith(handler)
    }
  }
}

package org.billstracker.utils

import org.log4s.Logger
import cats.effect.Effect

object LogsHelper {
  def trace[F[_] : Effect](t: Throwable)(msg: String)(implicit logger: Logger) = Effect[F].delay(logger.trace(t)(msg))
  def trace[F[_] : Effect](msg: String)(implicit logger: Logger) = Effect[F].delay(logger.trace(msg))

  def debug[F[_] : Effect](t: Throwable)(msg: String)(implicit logger: Logger) = Effect[F].delay(logger.debug(t)(msg))
  def debug[F[_] : Effect](msg: String)(implicit logger: Logger) = Effect[F].delay(logger.debug(msg))

  def info[F[_] : Effect](t: Throwable)(msg: String)(implicit logger: Logger) = Effect[F].delay(logger.info(t)(msg))
  def info[F[_] : Effect](msg: String)(implicit logger: Logger) = Effect[F].delay(logger.info(msg))

  def warn[F[_] : Effect](t: Throwable)(msg: String)(implicit logger: Logger) = Effect[F].delay(logger.warn(t)(msg))
  def warn[F[_] : Effect](msg: String)(implicit logger: Logger) = Effect[F].delay(logger.warn(msg))

  def error[F[_] : Effect](t: Throwable)(msg: String)(implicit logger: Logger) = Effect[F].delay(logger.error(t)(msg))
  def error[F[_] : Effect](msg: String)(implicit logger: Logger) = Effect[F].delay(logger.error(msg))
}

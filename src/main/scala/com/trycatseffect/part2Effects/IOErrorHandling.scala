package com.trycatseffect.part2Effects

import cats.effect.IO
import scala.util.Try
import scala.util.Success
import scala.util.Failure

object IOErrorHandling {

  val aFailure = IO(throw new RuntimeException("kuch to gadbad hai daya"))
  val anotherFailure =
    IO.raiseError(RuntimeException("iska matlab samjhe daya"))

  val handledFailure = aFailure.handleError { case _: RuntimeException =>
    "sab theek ho gya hai sir"
  }
  val handledFailureAgain = aFailure.handleErrorWith {
    case _: RuntimeException => IO("sab theek ko jayga sir")
  }

  // this converts an IO to IO of either type
  val effectAsEither: IO[Either[Throwable, String]] = aFailure.attempt

  val redeemFailure = anotherFailure.redeem(
    ex => s"Failure: $ex",
    value => s"Success: $value"
  )

  val redeemFailureWith = anotherFailure.redeemWith(
    ex => IO(s"Failure, $ex"),
    value => IO(s"Success: $value")
  )

}

object IOErrorHandlingExercises {

  // exercises

  // 1. construct potentially failed IOs from standard data-types  (Option, Either, Try)

  def option2IO[A](op: Option[A])(ifEmpty: Throwable): IO[A] = op match
    case Some(value) => IO(value)
    case None        => IO.raiseError(ifEmpty)

  def Try2IO[A](atry: Try[A]): IO[A] = atry match
    case Success(value) => IO(value)
    case Failure(exc) => IO.raiseError(exc)

  def either2IO[A](anEither: Either[Throwable, A]): IO[A] = anEither match
    case Left(thr) => IO.raiseError(thr)
    case Right(value) => IO(value) 
  

  // 2. handleError and handleErrorWith, implementations

  def handleError[A](io: IO[A])(handler: Throwable => A):IO[A] = ???

  def handleErrorWith[A](io: IO[A])(handler: Throwable => IO[A]):IO[A] = ???


  def main(args: Array[String]): Unit = {

  }
}

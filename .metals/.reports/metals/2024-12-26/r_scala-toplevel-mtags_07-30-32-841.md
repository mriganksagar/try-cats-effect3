error id: file://<WORKSPACE>/src/main/scala/com/trycatseffect/part2Effects/IOErrorHandling.scala:[1136..1139) in Input.VirtualFile("file://<WORKSPACE>/src/main/scala/com/trycatseffect/part2Effects/IOErrorHandling.scala", "package com.trycatseffect.part2Effects

import cats.effect.IO
import scala.util.Try

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


object IOErrorHandlingExercises{

    //exercises

    // 1. construct potentially failed IOs from standard data-types  (Option, Either, Try)

    def option2IO[A](op: Option[A])(ifEmpty: Throwable): IO[A] = {

    }

    def Try2IO[A](atry: Try[A]): IO[A] = ???

    def 
    def main(args: Array[String]): Unit = {

    }
}
")
file://<WORKSPACE>/file:<WORKSPACE>/src/main/scala/com/trycatseffect/part2Effects/IOErrorHandling.scala
file://<WORKSPACE>/src/main/scala/com/trycatseffect/part2Effects/IOErrorHandling.scala:48: error: expected identifier; obtained def
    def main(args: Array[String]): Unit = {
    ^
#### Short summary: 

expected identifier; obtained def
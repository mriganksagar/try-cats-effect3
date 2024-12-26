file://<WORKSPACE>/src/main/scala/com/trycatseffect/part2Effects/IOErrorHandling.scala
### scala.MatchError: TypeDef(A,TypeBoundsTree(EmptyTree,EmptyTree,EmptyTree)) (of class dotty.tools.dotc.ast.Trees$TypeDef)

occurred in the presentation compiler.

action parameters:
offset: 1500
uri: file://<WORKSPACE>/src/main/scala/com/trycatseffect/part2Effects/IOErrorHandling.scala
text:
```scala
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
  

  // 2.@@handleError and handleErrorWith, implementations

  def handleError[A](io: IO[A])(handler: Throwable => A):IO[A] = ???

  def handleErrorWith[A](io: IO[A])(handler: Throwable => IO[A]):IO[A] = ???


  def main(args: Array[String]): Unit = {

  }
}

```



#### Error stacktrace:

```
dotty.tools.pc.completions.KeywordsCompletions$.checkTemplateForNewParents$$anonfun$2(KeywordsCompletions.scala:220)
	scala.Option.map(Option.scala:242)
	dotty.tools.pc.completions.KeywordsCompletions$.checkTemplateForNewParents(KeywordsCompletions.scala:221)
	dotty.tools.pc.completions.KeywordsCompletions$.contribute(KeywordsCompletions.scala:46)
	dotty.tools.pc.completions.Completions.completions(Completions.scala:119)
	dotty.tools.pc.completions.CompletionProvider.completions(CompletionProvider.scala:87)
	dotty.tools.pc.ScalaPresentationCompiler.complete$$anonfun$1(ScalaPresentationCompiler.scala:143)
```
#### Short summary: 

scala.MatchError: TypeDef(A,TypeBoundsTree(EmptyTree,EmptyTree,EmptyTree)) (of class dotty.tools.dotc.ast.Trees$TypeDef)
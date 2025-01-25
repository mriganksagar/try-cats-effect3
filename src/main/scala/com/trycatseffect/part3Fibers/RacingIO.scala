package com.trycatseffect.part3Fibers

import cats.effect.IOApp
import cats.effect.IO
import scala.concurrent.duration._
import cats.effect.kernel.Outcome

import cats.effect.{OutcomeIO, FiberIO}
import cats.effect.kernel.Outcome.Succeeded
import cats.effect.kernel.Outcome.Canceled
import cats.effect.kernel.Outcome.Errored
import com.trycatseffect.utils.bebug

object RacingIO extends IOApp.Simple {

    import com.trycatseffect.utils._

    def runWithSleep[A](value: A, duration: FiniteDuration): IO[A] = {
        (
          IO("starting computation").bebug >>
              IO.sleep(duration) >>
              IO(s"computation done for value $value").bebug >>
              IO(value)
        ).onCancel(IO(s"computation cancelled for value $value").bebug.void)
    }

    // some IOs with sleep
    val meaningOfLife = runWithSleep(42, 1.second)
    val favLanguage = runWithSleep("Scala", 2.second)

    def testRace() = {
        val racedIOs: IO[Either[Int, String]] = IO.race(meaningOfLife, favLanguage)

        racedIOs.flatMap {
            case Left(value)  => IO("meaning of life won").bebug
            case Right(value) => IO("fav language won").bebug
        }
    }

    // with race pair we can tell what we want to do with losing effect in a race
    def testRacePair() = {
        val racedPairIO
            : IO[Either[(OutcomeIO[Int], FiberIO[String]), (FiberIO[Int], OutcomeIO[String])]] =
            IO.racePair(meaningOfLife, favLanguage)

        racedPairIO.flatMap {
            case Left((outMol, fibLang)) =>
                fibLang.cancel >> IO("meaning of life won").bebug >> IO(outMol).bebug
            case Right((fibMol, outLang)) =>
                fibMol.cancel >> IO("fav lang won").bebug >> IO(outLang).bebug
        }

    }
    override def run: IO[Unit] = testRacePair().void
}

object RacingExercises extends IOApp.Simple {

    // Exercise 1.
    // Implement a timeout pattern using 'race' this time

    def timeout[A](io: IO[A], duration: FiniteDuration): IO[A] =
        IO.race(io, IO.sleep(duration)).flatMap {
            case Left(value) => IO(value)
            case Right(_)    => IO.raiseError(new Exception("couldn't run in time"))
        }

    val oneEffect = IO.sleep(1.second) >> IO(42).bebug
    val timedOutEfect = timeout(oneEffect, 0.5.second) // cancell oneEffect
    val timedOutEfect_v2 = oneEffect.timeout(2.second) // same method but from Cats effect 

    // Exercise 2.
    // Implement a method to return losing effect from a race (using racepair)

    def unrace[A, B](ioa: IO[A], iob: IO[B]): IO[Either[A, B]] =
        IO.racePair(ioa, iob).flatMap {
            case Left((_, fibB)) =>
                fibB.join.flatMap {
                    case Succeeded(fa) => fa.map(x => Right(x))
                    case Canceled()    => IO.raiseError(new Exception("the io was cancelled"))
                    case Errored(e)    => IO.raiseError(e)
                }
            case Right((fibA, _)) =>
                fibA.join.flatMap {
                    case Succeeded(fa) => fa.map(x => Left(x))
                    case Canceled()    => IO.raiseError(new Exception("the io was cancelled"))
                    case Errored(e)    => IO.raiseError(e)
                }
        }

    // here I will use for comprehensions for better readability instead of flatmaps
    // not much better though and another thing to notice is that I used fold on Outcome instead of case matching
    def unrace_v2[A, B](ioa: IO[A], iob: IO[B]): IO[Either[A, B]] =
        for {
            raceIOs <- IO.racePair(ioa, iob)
            result <- raceIOs match {
                case Left((_, fibB)) =>
                    fibB.join.flatMap(
                      _.fold(
                        IO.raiseError(new Exception("the IO was cancelled")),
                        e => IO.raiseError(e),
                        _.flatMap(x => IO(Right[A, B](x)))
                      )
                    )
                case Right((fibA, _)) =>
                    fibA.join.flatMap(
                      _.fold(
                        IO.raiseError(new Exception("the IO was cancelled")),
                        e => IO.raiseError(e),
                        _.flatMap(x => IO(Left[A, B](x)))
                      )
                    )
            }
        } yield result

    // Exercise 3.
    // Implement Race from Race pair

    def simpleRace[A, B](ioa: IO[A], iob: IO[B]): IO[Either[A, B]] =
        IO.racePair(ioa, iob).flatMap {
            case Left(a, fibB) =>
                fibB.cancel >> a.fold(
                  IO.raiseError(new Exception("the IO was cancelled")),
                  e => IO.raiseError(e),
                  _.flatMap(x => IO(Left[A, B](x)))
                )
            case Right(fibA, b) =>
                fibA.cancel >> b.fold(
                  IO.raiseError(new Exception("the IO was cancelled")),
                  e => IO.raiseError(e),
                  _.flatMap(x => IO(Right[A, B](x)))
                )
        }

    // rock the jvm implemented it in this way:
    // if there is a cancellation from winning IO then return the second IO

    def simpleRace_v2[A, B](ioa: IO[A], iob: IO[B]): IO[Either[A, B]] =
        IO.racePair(ioa, iob).flatMap {
            case Left(outA, fibB) =>
                outA match {
                    case Succeeded(a) => fibB.cancel >> a.map(Left(_))
                    case Errored(e)   => IO.raiseError(e)
                    case Canceled() =>
                        fibB.join.flatMap {
                            case Succeeded(b) => b.map(Right(_))
                            case Errored(e)   => IO.raiseError(e)
                            case Canceled() =>
                                IO.raiseError(new RuntimeException(" Both computations failed"))
                        }
                }
            case Right(fibA, outB) =>
                outB match {
                    case Succeeded(a) => fibA.cancel >> a.map(Right(_))
                    case Errored(e)   => IO.raiseError(e)
                    case Canceled() =>
                        fibA.join.flatMap {
                            case Succeeded(b) => b.map(Left(_))
                            case Errored(e)   => IO.raiseError(e)
                            case Canceled() =>
                                IO.raiseError(new RuntimeException(" Both computations failed"))
                        }
                }
        }
    override def run: IO[Unit] = timedOutEfect_v2.void
}

// questions from my mind
// 1. what happens upstream when io: IO[A] throws error ?

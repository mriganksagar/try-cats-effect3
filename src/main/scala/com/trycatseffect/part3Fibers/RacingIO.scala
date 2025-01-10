package com.trycatseffect.part3Fibers

import cats.effect.IOApp
import cats.effect.IO
import cats.instances.duration
import scala.concurrent.duration._
import com.trycatseffect.part2Effects.IOParallelism.favLanguage
import cats.effect.kernel.Outcome

import cats.effect.{OutcomeIO, FiberIO}
import cats.effect.kernel.Outcome.Succeeded
import cats.effect.kernel.Outcome.Canceled
import cats.effect.kernel.Outcome.Errored

object RacingIO extends IOApp.Simple {

    import com.trycatseffect.utils._

    def runWithSleep[A](value: A, duration: FiniteDuration): IO[A] = {
        (
          IO("starting computation").ownDebug >>
              IO.sleep(duration) >>
              IO(s"computation done for value $value").ownDebug >>
              IO(value)
        ).onCancel(IO(s"computation cancelled for value $value").ownDebug.void)
    }

    // some IOs with sleep
    val meaningOfLife = runWithSleep(42, 1.second)
    val favLanguage = runWithSleep("Scala", 2.second)

    def testRace() = {
        val racedIOs: IO[Either[Int, String]] = IO.race(meaningOfLife, favLanguage)

        racedIOs.flatMap {
            case Left(value)  => IO("meaning of life won").ownDebug
            case Right(value) => IO("fav language won").ownDebug
        }
    }

    // with race pair we can tell what we want to do with losing effect in a race
    def testRacePair() = {
        val racedPairIO
            : IO[Either[(OutcomeIO[Int], FiberIO[String]), (FiberIO[Int], OutcomeIO[String])]] =
            IO.racePair(meaningOfLife, favLanguage)

        racedPairIO.flatMap {
            case Left((outMol, fibLang)) =>
                fibLang.cancel >> IO("meaning of life won").ownDebug >> IO(outMol).ownDebug
            case Right((fibMol, outLang)) =>
                fibMol.cancel >> IO("fav lang won").ownDebug >> IO(outLang).ownDebug
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

    // Exercise 2.
    // Implement a method to return losing effect from a race (using racepair)

    def unrace[A, B](ioa: IO[A], iob: IO[B]): IO[Either[A, B]] =
        IO.racePair(ioa, iob).flatMap {
            case Left((_, fibB)) =>
                fibB.join.flatMap {
                    case Succeeded(fa) => fa.flatMap(x => IO(Right(x)))
                    case Canceled()    => IO.raiseError(new Exception("the io was cancelled"))
                    case Errored(e)    => IO.raiseError(e)
                }
            case Right((fibA, _)) =>
                fibA.join.flatMap {
                    case Succeeded(fa) => fa.flatMap(x => IO(Left(x)))
                    case Canceled()    => IO.raiseError(new Exception("the io was cancelled"))
                    case Errored(e)    => IO.raiseError(e)
                }
        }

    // here I will use for comprehensions for better readability instead of flatmaps
    def unrace_v2[A, B](ioa: IO[A], iob: IO[B]): IO[Either[A, B]] =
        for {
            raceIOs <- IO.racePair(ioa, iob)
            result <- raceIOs match {
                case Left((_, fibB))  => 
                    fibB.join.flatMap( _.fold(
                        IO.raiseError(new Exception("the IO was cancelled")),
                        e => IO.raiseError(e),
                        _.flatMap(x => IO(Right[A, B](x))))
                    )
                case Right((fibA, _)) => 
                    fibA.join.flatMap( _.fold(
                        IO.raiseError(new Exception("the IO was cancelled")),
                        e => IO.raiseError(e),
                        _.flatMap(x => IO(Left[A, B](x))))
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
    override def run: IO[Unit] = ???
}

// questions from my mind
// 1. what happens upstream when io: IO[A] throws error ?

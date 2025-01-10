package com.trycatseffect.part3Fibers

import cats.effect.IOApp
import cats.effect.IO
import cats.instances.duration
import scala.concurrent.duration._
import com.trycatseffect.part2Effects.IOParallelism.favLanguage
import cats.effect.kernel.Outcome

import cats.effect.{OutcomeIO, FiberIO}

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
        val racedPairIO: IO[Either[
            (OutcomeIO[Int], FiberIO[String]),
            (FiberIO[Int], OutcomeIO[String])]
            ] = IO.racePair(meaningOfLife, favLanguage)

        racedPairIO.flatMap{
            case Left((outMol, fibLang)) => fibLang.cancel >> IO("meaning of life won").ownDebug >> IO(outMol).ownDebug
            case Right((fibMol, outLang)) => fibMol.cancel >> IO("fav lang won").ownDebug >> IO(outLang).ownDebug  
        }
        

    }
    override def run: IO[Unit] = testRacePair().void
}

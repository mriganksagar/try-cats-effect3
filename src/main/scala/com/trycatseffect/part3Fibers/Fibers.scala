package com.trycatseffect.part3Fibers

import cats.effect.IOApp
import cats.effect.IO
import com.trycatseffect.utils.ownDebug
import cats.effect.kernel.Outcome.{Succeeded, Canceled, Errored}
import scala.concurrent.duration._

object Fibers {
    val meaningOfLife = IO.pure(42)
    val favLanguage = IO.pure("scala")
    
    // How to create a fiber
    val aFiber = for {
        fib <- meaningOfLife.ownDebug.start // create a fiber
        fib2 <- favLanguage.ownDebug.start
        result <- fib.join // join a fiber or wait till its completion, It is an IO itself
        _ <- fib2.cancel // cancel a fiber, an IO that cancels fiber
    } yield result
}


object FibersExercise extends IOApp.Simple{

    // Exercise 1. Write a function that runs an IO on another thread, and depending upon the result of fiber
    //  - return the result in an IO
    //  - if error or cancelled return a failed IO

    def processResultsFromFiber[A](io:IO[A]):IO[A] = {
        val fiberIO = for {
            fib <- io.start
            result <- fib.join
        } yield result
        
        fiberIO.flatMap{
            case Succeeded(fa) => fa
            case Errored(e) => IO.raiseError(e)
            case Canceled() => IO.raiseError(new RuntimeException("computation cancelled"))
        }
    }

    def testProcessResultsFromFiber() = {
        val anIO = IO("starting").ownDebug >> IO.sleep(1.second) >> IO("done").ownDebug >> IO(42).ownDebug
        processResultsFromFiber(anIO).void
    }
    // Exercise 2. Write a function that takes Two IOs ioa and iob, 
    // If both successful IO, tuple result
    // If first one fails, raise its error ignoring the second one's result/ error
    // second fails, return its error
    // both fails runtime exception

    def tupleIOs[A,B](ioa: IO[A], iob: IO[B]): IO[(A, B)] = {
        val result = for {
            fiba <- ioa.start
            fibb <- iob.start
            result1 <- fiba.join
            result2 <- fibb.join
        } yield (result1, result2)

        import cats.syntax.apply._ // for (a, b).tupled

        result.flatMap{
            case (Succeeded(a), Succeeded(b)) => (a, b).tupled
            case (Errored(a), _) => IO.raiseError(a)
            case (_, Errored(b)) => IO.raiseError(b)
            case _ => IO.raiseError(new RuntimeException("unexpected error occured while running")) 
        }
    }

    def testTupleIOs() = {
        val firstIO = IO.sleep(1.second) >> IO(1).ownDebug
        val secondIO = IO.sleep(500.milli) >> IO(2).ownDebug
        tupleIOs(firstIO, secondIO).ownDebug.void
    }
    // Exercise 3. Write a function that adds a timeout to an IO
    // returns IO if succeed before timeout
    // exception if failed before timeout
    // runtime if cancelled by timeout

    def timeout[A](io: IO[A], duration: FiniteDuration) = {
        val computation = for {
            fib <- io.start // should start the io
            _ <- IO.sleep(duration) >> fib.cancel // start another IO that sleeps , wakes up, and cancels the first fiber 
            result <- fib.join // get the result from first fiber by joining
        } yield result

        computation.flatMap{
            case Succeeded(fa) => fa
            case Errored(e) => IO.raiseError(e)
            case Canceled() => IO.raiseError(new RuntimeException("Couldn't complete in time"))
        }
    }

     override def run: IO[Unit] = testTupleIOs()
} 
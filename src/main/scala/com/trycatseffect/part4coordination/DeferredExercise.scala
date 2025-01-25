package com.trycatseffect.part4coordination

import cats.effect.{IO, IOApp, Ref, OutcomeIO, FiberIO}
import com.trycatseffect.utils._
import scala.concurrent.duration._
import cats.effect.kernel.Deferred

object DeferredExercise extends IOApp.Simple{

    /* 
        Exercise 1.
         - write a small alarm notificatin with two simultaneous IOs
            - one that increments a counter every second (a clock)
            - one that waits for the counter to become 10, then prints a message "subah ho gyi mamu"

        Solution: I used ticker and alarm programs ran both on fibers using ref for counter increments
        and deferred for signal
     */
    import cats.syntax.parallel._ // for .parTraverse method

    def alarmClock(): IO[Unit] = {
        def ticker(ticks: Ref[IO, Int], signal: Deferred[IO, Boolean]): IO[Unit] = 
            for {
                _ <- IO("tick").bebug
                _ <- IO.sleep(1.second)
                newTicks <- ticks.updateAndGet(_+1)
                _ <- if newTicks == 10 then signal.complete(true) else ticker(ticks, signal)
            } yield ()


        def alarm(signal: Deferred[IO, Boolean]): IO[String] = IO(s"alarm is set").bebug >> signal.get >> IO(s"Subah ho gyi mamu").bebug

        // program
        for {
            ticks <- IO.ref(0)
            signal <- IO.deferred[Boolean]
            _ <- List(ticker(ticks, signal), alarm(signal)).parSequence
            
            // upper line can be replaced by code below as well
            // fibTicker <- ticker(ticks, signal).start
            // fibAlarm <- alarm(signal).start
            // _ <- fibAlarm.join
            // _ <- fibTicker.join
        } yield ()
    }

    /* 
        Exercise 2 (Hard).
        Define our own RacePairWith method using deferred mechanism

        what about cancellation behaviour
        => 
            In case both IOs take infinite or long time for some reason and we want to cancel it
            our fiber should not wait for a signal
            another challenge is to manage started fibers

            for this, 
            I make it uncancellable with well though cancelling part and callbacks
     */

    def ourRacePairWith[A, B](ioa: IO[A], iob: IO[B]): IO[Either[
        (OutcomeIO[A], FiberIO[B]),
        (FiberIO[A], OutcomeIO[B])
    ]] = IO.uncancelable{ poll =>
        for{
            signal <- IO.deferred[Either[OutcomeIO[A], OutcomeIO[B]]]
            fibA <- ioa.guaranteeCase(outA => signal.complete(Left(outA)).void).start
            fibB <- iob.guaranteeCase(outB => signal.complete(Right(outB)).void).start
            result <- poll(signal.get).onCancel{
                // (fibA.cancel, fibB.cancel).parSequence.void // also a possible code
                for {
                    fibAc <- fibA.cancel.start
                    fibBc <- fibB.cancel.start
                    _ <- fibAc.join
                    _ <- fibBc.join
                } yield ()
            }
        } yield result match {
            case Left(outA) => Left(outA, fibB)
            case Right(outB) => Right(fibA, outB)  
        }
    }    
    override def run: IO[Unit] = alarmClock()
}

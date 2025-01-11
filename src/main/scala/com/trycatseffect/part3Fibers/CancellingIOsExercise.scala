package com.trycatseffect.part3Fibers

import cats.effect.IOApp
import cats.effect.IO
import com.trycatseffect.utils.ownDebug
import scala.concurrent.duration._

object CancellingIOsExercise extends IOApp.Simple{
    import CancellingIOs._

    /* 
        Exercise 1.

        Uncancellable with eliminate all cancel points except where Poll is used 
     */

    val cancelBeforeMeaningOfLife = IO.canceled >> IO(42).ownDebug
    val uncancelableMeaningOfLife = IO.uncancelable(_ => cancelBeforeMeaningOfLife)

    /* 
        Exercise 2.

        what will happen when cancellable poll is wrapped in uncancellable
        it will make it uncancellable
     */

    val authProgram_invincible = for {
        fib <- authFlow_partially_cancellable.uncancelable.start
        _ <- IO.sleep(1.second) >> IO("attempting cancel").ownDebug >> fib.cancel
    } yield ()

    /*  
        Exercise 3.
        Guest the output and behaviour
    
        Here on a 1.5 second sleep and fib cancel 
        what happens is that anything that has run is run, but if the effects after can be stopped , they will be stopped

        lets say we use 2.5 second duration then the 3rd sleep shall be running when cancel is called 
        so 2nd poll has started run and already has run and currenly is in sleep but the cancellable end will be cancelled as it can be
        and so will be the other cancelable effects following it

        one 1.5 second 2nd sleep will be running but uncancellable end will run and other effects following it that are inside poll 
        will be cancelled
    */
    def threeStepProgram = {
        val sequence = IO.uncancelable{ poll =>
            poll( IO("cancellable 1").ownDebug >> IO.sleep(1.second) >> IO("cancelable end 1").ownDebug) >> 
            IO("Uncancellable").ownDebug >> IO.sleep(1.second) >> IO("uncancelable end").ownDebug >>
            poll(IO("cancellable 2").ownDebug >> IO.sleep(1.second) >> IO("cancelable 2 end").ownDebug)
        }

        for {
            fib <- sequence.start
            _ <- IO.sleep(1.5.second) >> IO("attemptiong Cancelling").ownDebug >> fib.cancel
        } yield ()
    }
    override def run: IO[Unit] = threeStepProgram
}

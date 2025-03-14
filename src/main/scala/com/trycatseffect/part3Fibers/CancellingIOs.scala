package com.trycatseffect.part3Fibers

import cats.effect.IOApp
import cats.effect.IO
import com.trycatseffect.utils.bebug
import scala.concurrent.duration._

object CancellingIOs extends IOApp.Simple{

    /* 
        Cancelling IOs
        - fib.cancel
        - IO.race and other APIs
        - manual cancellation
     */

    // Anything composed after canceled will not be evaluated 
    val chainOfIOs = IO("waiting").bebug >> IO.canceled >> IO(42).bebug

    // lets create an IO with oncancel
    val onePayment = (
        IO("Payment running, don't cancel it.").bebug >>
        IO.sleep(1.second) >>
        IO("Payment completed.").bebug
    ).onCancel(IO("How Dare you cancel?").bebug.void)

    val readyForDoom = for {
        fib <- onePayment.start
        _ <- IO.sleep(0.5.second) >> fib.cancel
    }  yield ()

    // How to prevent a cancellation of a fiber 
    // # Uncancelable

    val atomicPayment = IO.uncancelable(_ => onePayment)
    val atomicPayment_v2 = onePayment.uncancelable

    val canNotDoom = for {
        fib <- atomicPayment.start
        _ <- IO.sleep(0.5.second) >> IO("attempt to cancel").bebug >>fib.cancel
    }  yield ()

    /* 
        The Uncancelable API is complex and takes a function from POLL[IO] => IO
        its superpower is to mark sections that can cancel and that can not
     */

    /* 
        Example: Authentication Service. Make it uncancellable (fully or partially)
        1. take input data
        2. validate data to authorise or authenticate

        - later we use a login flow and a program to try to cancel the flow
     */

    val getUserData: IO[String] = IO("Getting user data").bebug >> IO.sleep(2.second) >> IO("data input completed").bebug >> IO("I am Batman")
    val validateUserData = (data: String) => IO("Verifying User Data").bebug >> IO.sleep(2.second) >> IO("Done verifying").bebug >>IO(data == "I am Batman")

    // Lets create an IO that has flow of input and validate, and wrap it in uncancellable
    val authFlow = IO.uncancelable{ poll =>
        for {
            data <- getUserData.onCancel(IO("Authentication service timed out").bebug.void)
            isVerified <- validateUserData(data)
            _ <- if isVerified then IO("Authentication Successful").bebug else IO("Authentication Failed").bebug
        }yield ()
    }

    val authProgram = for {
        fib <- authFlow.start
        _ <- IO.sleep(3.second) >> fib.cancel
    } yield ()

    /* 
        with above program I can't cancel the whole program
        however sometimes, we might need to make a portion cancellable
        
        we can use *poll* for that
     */

    val authFlow_partially_cancellable = IO.uncancelable{ poll =>
        for {
            data <- poll(getUserData.onCancel(IO("Authentication service timed out").bebug.void))
            isVerified <- validateUserData(data)
            _ <- if isVerified then IO("Authentication Successful").bebug else IO("Authentication Failed").bebug
        }yield ()
    }   

    val authProgram_partially_cancellable = for {
        fib <- authFlow_partially_cancellable.start
        _ <- IO.sleep(1.second) >> IO("attempting cancel").bebug >> fib.cancel // wont cancel at 3 seconds but at 1 second , it will
    } yield ()
    override def run: IO[Unit] = authProgram_partially_cancellable
}

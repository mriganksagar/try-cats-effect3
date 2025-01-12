package com.trycatseffect.part3Fibers

import cats.effect.IO
import cats.effect.IOApp
import scala.concurrent.duration._
import com.trycatseffect.utils._
import scala.concurrent.ExecutionContext
import java.util.concurrent.Executors
object BlockingIOs extends IOApp.Simple{

    /* 
        When we use IO.sleep, how does the blocking works ?
        ->
        It is called Semantic blocking. The real thread (JVM thread) is not blocked
        instead it is an even based runtime where after 1 second next IOs will be assigned to thread pool

        when we use IO.sleep, it yield control over the thread
        that's why after a sleep next effects might run on different threads on the pool (but not necessarily)
     */
    val someSleeps = for {
        _ <- IO.sleep(1.second).ownDebug // SEMANTIC BLOCKING
        _ <- IO.sleep(1.second).ownDebug
    } yield ()

    /* 
        IO.blocking makes the thread really blocking.
        to test the theory
        no matter how many times we run this code always both output shows same thread number

        Another thing to notice : 
            the Blocking thread run on a separate thread pool just for Blocking IOs
     */ 
    val aBlockingIO = IO.blocking{
        println(s"[${Thread.currentThread().getName()}] starting a blocking code")
        Thread.sleep(1000)
        println(s"[${Thread.currentThread().getName()}] computed a blocking code")
    }

    /* 
        to manually yield control of a thread, just like IO.sleep yield control
        cats effect provide an IO for it * IO.cede*

        Though on running every log is runs on same thread but
        it is due to cats runtime, even after cede it reassign it to that same thread due to performance reasons
     */

    val iosOnManyThreads = for {
        _ <- IO("first").ownDebug
        _ <- IO.cede
        _ <- IO("second").ownDebug
        _ <- IO.cede
        _ <- IO("third").ownDebug
    } yield ()

    val aThousandCedes = (1 to 1000).map(IO.pure(_).ownDebug).reduce(_ >> IO.cede >> _)

    def testThousandCedesOnCustomThreadPool = {
        val ec: ExecutionContext = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(8))
        aThousandCedes.evalOn(ec)
    }
    override def run: IO[Unit] = testThousandCedesOnCustomThreadPool.void
}

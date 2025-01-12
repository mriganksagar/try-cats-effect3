package com.trycatseffect.part3Fibers

import cats.effect.IOApp
import cats.effect.IO
import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Try, Success}
import java.lang.invoke.CallSite
import com.trycatseffect.utils.ownDebug

object AsyncIOs extends IOApp.Simple {

    // IOs can asynchronously on fibers, without having to manually manage the fiber lifecycle

    val threadPool = Executors.newFixedThreadPool(8)
    given ec: ExecutionContext = ExecutionContext.fromExecutorService(threadPool) // making it given to save time
    type Callback[A] = Either[Throwable, A] => Unit


    def computeMeaningOfLife(): Int = {
        Thread.sleep(1000)
        println(s"[${Thread.currentThread().getName()}] computing the meaning of life on a thread pool")
        42
    }
    def computeMeaningOfLifeEither(): Either[Throwable, Int] = Try{
        computeMeaningOfLife()
    }.toEither

    /* 
        We have a method that computes something on separate thread pool
        but now the interesting question is
        how do we use computed result inside an IO
        that is where IO.async and IO.async_ comes in

        by using async, the cats effect thread is semantically blocked until
        callback is called with the result 
     */

    val asyncIOMol: IO[Int] = IO.async_{ cb => 
        threadPool.execute{ () =>
            val result = computeMeaningOfLifeEither()
            cb(result) // cats effect thread is notified of the result
        }
    }

    /**
      * Exercise 1.
      * Write a method to abstract the boilerplate code to 
      * make an asynchronous IO out of a computation and execution context
      */

    def asyncToIO[A](computation: () =>  A)(ec: ExecutionContext): IO[A] = {
        IO.async_{ cb =>
            ec.execute{ () =>
                val result = Try{computation()}.toEither
                cb(result)
            }
        }
    }

    val asyncIOMol_v2: IO[Int] = asyncToIO(computeMeaningOfLife)(ec)
    
    /* 
        Exercise 2.
        Lift Scala Future into IO

        => 
            accepting future as call by name is important to delay its execution
            and for that we should use def or lazy val while calling method instead of val
     */

    def asyncIOFromFuture[A](future: =>  Future[A]): IO[A] = {
        IO.async_{ cb => 
            future.onComplete{t => cb(t.toEither)}(ec)
        }
    }
    
    lazy val molFuture: Future[Int] = Future{computeMeaningOfLife()}(ec)

    val asyncIOMol_v3: IO[Int] = asyncIOFromFuture(molFuture)

    val asyncIOMol_v4: IO[Int] = IO.fromFuture(IO(molFuture))

    /* 
        Exercise 3. A never Ending IO, 
        using the async construct

        =>
            By not calling the callback ever the IO will never be signalled
     */

    val aNeverEndingIO = IO.async_(_=>())
    val aNeverEndingIO_v2 = IO.never
    override def run: IO[Unit] = asyncIOMol_v4.ownDebug >> IO(threadPool.shutdown())
}

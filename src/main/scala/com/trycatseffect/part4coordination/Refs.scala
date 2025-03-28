package com.trycatseffect.part4coordination

import cats.effect.{IO, IOApp, Ref}

import com.trycatseffect.utils.bebug
object Refs extends IOApp.Simple {

    /*
        ref = purely functional atomic reference
        ref gives thread safe operations
     */

    // create refs
    val atomicMol: IO[Ref[IO, Int]] = Ref[IO].of(42)
    val atomicMol_v2: IO[Ref[IO, Int]] = IO.ref(42)

    // operations on refs

    val setOperationDemo: IO[Unit] = atomicMol.flatMap(ref => ref.set(43)) // returns unit IO
    val getOperationDemo: IO[Int] = atomicMol.flatMap(ref => ref.get) // used to get the value
    val gsMol = atomicMol.flatMap(_.getAndSet(43)) // get and then set value, returns old value
    val updateMol =
        atomicMol.flatMap(_.update(value => value + 4)) // set value but return new value
    val getUpdateMol =
        atomicMol.flatMap(_.getAndUpdate(value => value + 42)) // update value but return old value
    val updateGetMol =
        atomicMol.flatMap(_.updateAndGet(value => value + 43)) // update value but return new value

    // modifying ref value but return a different value in IO

    val modifyMol: IO[String] = atomicMol_v2.flatMap { ref =>
        ref.modify(value => (value * 10, s"the current value is $value"))
    }

    /*
        why we need atomic values
        to write concurrent and thread safe read/writes over shared data structre
        speciality: it is purely functional
     */

    def demoConcurrentWorkImpure: IO[Unit] = {
        import cats.syntax.parallel._

        var count = 0

        /*
            this program doesn't work well with correct output
            if concurrent threads IO run that try to change count due to non atomic writes
         */
        def task(workload: String): IO[Unit] = {
            val wordcount = workload.split(" ").length
            for {
                _ <- IO(s"counting words for '$workload' : $wordcount").bebug
                newCount = count + wordcount
                _ <- IO(s"new total words count: $newCount").bebug
                _ = count = newCount
            } yield ()
        }

        List(
          "mera naam shaktimaan",
          "I love cats effect",
          "tujhe bhoola dia fir kyu teri yaado ne muje rula dia"
        )
            .map(task)
            .parSequence
            .void
    }

    def demoConcurrentWorkWithAtomic: IO[Unit] = {
        import cats.syntax.parallel._

        def task(counter: Ref[IO, Int])(workload: String) = {
            val wordcount = workload.split(" ").length
            for {
                _ <- IO(s"counting words for '$workload' : $wordcount").bebug
                newCount <- counter.updateAndGet(_ + wordcount)
                _ <- IO(s"new total words count: $newCount").bebug
            } yield ()
        }

        val titles = List(
          "mera naam shaktimaan",
          "I love cats effect",
          "tujhe bhoola dia fir kyu teri yaado ne muje rula dia"
        )

        for {
            counter <- IO.ref(0)
            _ <- titles.map(task(counter)).parSequence
            finalCount <- counter.get
            _ <- IO(s"the final count is $finalCount").bebug
        } yield ()
    }
    override def run: IO[Unit] = demoConcurrentWorkWithAtomic
}

import scala.concurrent.duration._ 
import cats.syntax.parallel._ // used for parTupled method

object RefsExercise extends IOApp.Simple {

    /*
        Exercise 1.
        lets say if have a var ticks, and two methods or IOs
        one IO increase tick every one second
        one IO print tick every 5 seconds
        both IOs are running parallely

        identify problem in the method in using a non ref or mutable variable
        and construct a concurrent approach using refs

        => the problem would be in mutating var or write by two programs
        though in this program one thread is reading and one is writing so that's not any problem unless we make two threads write
     */


    def tickingClock(): IO[Unit] = {
        for {
            tick <- IO.ref(0L)
            _ <- {
                val tickingProgram = (IO.sleep(1.second) >> tick.update(_ + 1)).foreverM
                val printingProgram = (IO.sleep(5.second) >> tick.get.flatMap(currentTicks =>
                    IO(s"Ticks: $currentTicks").bebug
                )).foreverM

                (tickingProgram, printingProgram).parTupled
            }
        } yield ()
    }
    override def run: IO[Unit] = tickingClock()
}

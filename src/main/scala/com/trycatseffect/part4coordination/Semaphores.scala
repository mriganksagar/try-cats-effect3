package com.trycatseffect.part4coordination
import cats.effect.{IO, IOApp}
import cats.effect.std.Semaphore
import scala.concurrent.duration._
import scala.util.Random
import com.trycatseffect.utils.bebug
import cats.syntax.parallel._

object Semaphores extends IOApp.Simple {

    def aTask: IO[Int] = IO.sleep(1.second) >> IO(Random.nextInt(1000))

    def loginAndCompute[T](id: Int, sem: Semaphore[IO], aTask: IO[T]): IO[T] = for {
        _ <- IO(s"[SessionId: $id], starting ").bebug
        _ <- sem.acquire
        // critical section
        _ <- IO(s"[SessionId: $id], logged-in and computing").bebug
        result <- aTask
        _ <- IO(s"[SessionId: $id], computed stuff and logging out").bebug
        _ <- sem.release
    } yield result

    def loginAndComputeWeighted[T](id: Int, wt: Int, sem: Semaphore[IO], aTask: IO[T]): IO[T] =
        for {
            _ <- IO(s"[SessionId: $id], starting to acquire $wt resources").bebug
            _ <- sem.acquireN(wt)
            // critical section
            _ <- IO(s"[SessionId: $id], logged-in and computing").bebug
            result <- aTask
            _ <- IO(s"[SessionId: $id], computed stuff and logging out").bebug
            _ <- sem.releaseN(wt)
        } yield result

    def demoLoginAndCompute: IO[Unit] = for {
        sem <- Semaphore[IO](2)
        _ <- (
          loginAndCompute(1, sem, aTask),
          loginAndCompute(2, sem, aTask),
          loginAndCompute(3, sem, aTask)
        ).parTupled
    } yield ()

    def demoLoginAndComputeWeighted: IO[Unit] = for {
        sem <- Semaphore[IO](6)
        _ <- (
          loginAndComputeWeighted(1, 2, sem, aTask),
          loginAndComputeWeighted(2, 5, sem, aTask),
          loginAndComputeWeighted(3, 3, sem, aTask)
        ).parTupled
    } yield ()


    // when we have a semaphore of 1 we have a mutex

    val mutex = Semaphore[IO](1)
    val program_20usersLoggingIn = mutex.flatMap{ mutex =>
        (1 to 20).toList.parTraverse( id => loginAndCompute(id, mutex, aTask))
    }

    override def run = program_20usersLoggingIn.bebug.void
}

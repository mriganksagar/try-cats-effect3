package com.trycatseffect.part2Effects

import cats.effect.{IO, IOApp}
import scala.io.StdIn
import cats.effect.ExitCode

object IOApps {
    val program = for {
        _ <- IO(println("what is your name, sire ?"))
        name <- IO(StdIn.readLine())
        _ <- IO(println(s"Hello $name"))
    } yield ()
}

object TestApp{
    import IOApps._
    import cats.effect.unsafe.implicits.global

    def main(args: Array[String]):Unit = {
        program.unsafeRunSync()
    }
}

object TestApp2 extends IOApp{
    import IOApps._
    override def run(args: List[String]): IO[ExitCode] = program.as(ExitCode.Success)
}


object MySimpleApp extends IOApp.Simple{
    import IOApps._
    override def run: IO[Unit] = program
}
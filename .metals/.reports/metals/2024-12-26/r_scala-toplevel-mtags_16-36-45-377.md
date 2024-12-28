error id: file://<WORKSPACE>/src/main/scala/com/trycatseffect/part2Effects/IOApps.scala:[654..655) in Input.VirtualFile("file://<WORKSPACE>/src/main/scala/com/trycatseffect/part2Effects/IOApps.scala", "package com.trycatseffect.part2Effects

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


object MySimpleApp extends IOApp.{
    import IOApps._
    override def run(args: List[String]): IO[ExitCode] = program.as(ExitCode.Success)
}")
file://<WORKSPACE>/file:<WORKSPACE>/src/main/scala/com/trycatseffect/part2Effects/IOApps.scala
file://<WORKSPACE>/src/main/scala/com/trycatseffect/part2Effects/IOApps.scala:30: error: expected identifier; obtained lbrace
object MySimpleApp extends IOApp.{
                                 ^
#### Short summary: 

expected identifier; obtained lbrace
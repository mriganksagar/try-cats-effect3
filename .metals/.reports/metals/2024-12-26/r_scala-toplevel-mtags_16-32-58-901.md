error id: file://<WORKSPACE>/src/main/scala/com/trycatseffect/part2Effects/IOApps.scala:[472..472) in Input.VirtualFile("file://<WORKSPACE>/src/main/scala/com/trycatseffect/part2Effects/IOApps.scala", "package com.trycatseffect.part2Effects

import cats.effect.IO
import scala.io.StdIn

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

object TestApp2 extends cats.")
file://<WORKSPACE>/file:<WORKSPACE>/src/main/scala/com/trycatseffect/part2Effects/IOApps.scala
file://<WORKSPACE>/src/main/scala/com/trycatseffect/part2Effects/IOApps.scala:23: error: expected identifier; obtained eof
object TestApp2 extends cats.
                             ^
#### Short summary: 

expected identifier; obtained eof
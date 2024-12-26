package com.trycatseffect.part2Effects

import cats.effect.IO
import scala.io.StdIn

object IOIntro {
  val firstIO = IO.pure(43)
  // IO pure is eager and should not have any effect associated

  val ourFirstDelayedIO = IO.delay({
    println("hey im inside a delay giving off an integer")
    54
  })

  val ourFirstDelayedIO2 = IO{
    println("hey there delilah")
    44
  }

  val tenTimesFirstIO = firstIO.map(_*10)
  val printingFirstIO = firstIO.flatMap( v => IO(println(v)))

  val combinedProgramWithIO = for {
    x <- IO{StdIn.readLine()}
    y <- IO{StdIn.readLine()}
    _ <- IO(println(x+y))
  } yield ()


  import cats.syntax.apply._

  val combinedIO = (firstIO, tenTimesFirstIO).mapN(_+_)

  val inputIO = IO(StdIn.readLine())
  val combinedProgramWithIO2 = (inputIO, inputIO).mapN(_+_).map(println)


  def main(args:Array[String]):Unit = {
    import cats.effect.unsafe.implicits.global
    
    firstIO.unsafeRunSync()
    ourFirstDelayedIO.unsafeRunSync()

    combinedProgramWithIO.unsafeRunSync()

    println(combinedIO.unsafeRunSync())
    combinedProgramWithIO2.unsafeRunSync()

  }
}

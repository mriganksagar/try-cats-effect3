package com.trycatseffect.part2Effects

import scala.io.StdIn.readLine

case class MyIO[A](unsafeRun: () => A) {
  def map[B](f: A => B): MyIO[B] = {
    MyIO(() => f(unsafeRun()))
  }

  def flatMap[B](f: A => MyIO[B]): MyIO[B] = {
    MyIO(() => f(unsafeRun()).unsafeRun())
  }
}

object EffectsExercises {
  // 1. An IO which returns the current time of the system
  val currentTimeIO = MyIO { () => System.currentTimeMillis() }

  // 2. An IO which measures the duration of a computation
  def measure1[A](computation: MyIO[A]): MyIO[Long] = MyIO { () =>
    {
      val timebefore = currentTimeIO.unsafeRun()
      computation.unsafeRun()
      val timeafter = currentTimeIO.unsafeRun()
      timeafter - timebefore
    }
  }

  // another but a better solution using for comprehension

  def measure[A](computation: MyIO[A]): MyIO[Long] = for {
    timebefore <- currentTimeIO
    _ <- computation
    timeafter <- currentTimeIO
  } yield timeafter - timebefore

  // 3. An IO that prints something to the console
  def printIO(value: String) = MyIO { () => println(value) }

  // 4. An IO that reads a line from the standard input
  val readIO = MyIO[String] { () => readLine() }

  def main(args: Array[String]) = {
    println(currentTimeIO.unsafeRun())
    println(measure(MyIO(() => {
      Thread.sleep(100)
    })).unsafeRun())

    def testConsole() = {
      val test = for {
        x <- readIO
        y <- readIO
        _ <- printIO(x + y)
      } yield ()
      test.unsafeRun()
    }

    testConsole()
  }
}

package com.trycats

import cats.Semigroupal

object ParallelTryout {

    import cats.syntax.apply._
    import cats.instances.either._
    import cats.instances.string._
    import cats.instances.vector._

    def SimpleUsage:Unit = {
        val error1 = Left("Error Alok")
        val error2 = Left("Error Jalok")

        // println((error1, error2).tupled)

        val error3: Either[Vector[String], _] = Left(Vector("Error Alok"))
        val error4: Either[Vector[String], _] = Left(Vector("Error Jalok"))

        println(Semigroupal[Either[String, *]].product(error1, error2))
        
        type ErrorOr[A] = Either[String, A]
        val error5: ErrorOr[Int] = Left("Error 1")
        val error6: ErrorOr[Int] = Left("Error 2")
        
        val tupledError2 = (error5, error6).tupled

        val tupledError3 = (error3, error4).tupled

        // this won't compile, due to unavailability of Semigroupal instance for Left so we need to specify either
        // val tupledError4 = (error1, error2).tupled
    }

    def parallelUsage: Unit = {
        val error1: Either[String, _] = Left("Error Alok ")
        val error2: Either[String, _]= Left("Error Jalok ")
        
        // imports necessary for below functionality
        import cats.syntax.parallel._

        val parTupledError = (error1, error2).parTupled
        println(parTupledError)
    }

    def main(args: Array[String]): Unit = {
        // SimpleUsage
        parallelUsage
    }
}
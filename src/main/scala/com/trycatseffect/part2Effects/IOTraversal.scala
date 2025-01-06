package com.trycatseffect.part2Effects

import cats.effect.IOApp
import cats.effect.IO
import cats.Traverse
import scala.util.Random
import com.trycatseffect.utils.ownDebug
import cats.Parallel

object IOTraversal extends IOApp.Simple{

    def computeAsIO(s: String):IO[Int] = IO{
        Thread.sleep(Random.nextInt(1000))
        s.strip().split(" ").length
    }

    val movies = List("mera naam joker", "dil chahta hai", "zindagi na milegi dubara", "3 idiots")


    def showcaseIOofList = {
        Traverse[List].traverse(movies)(computeAsIO.andThen(ownDebug))
    }
    
    def showcaseIOofListParallely = {
        Parallel.parTraverse(movies)(computeAsIO.andThen(ownDebug))
    }

    // Here in Below methods, I will use extension methods / implicit classes instead of Object Parallel or Traverse
    def showcaseIOofList_v2 = {
        import cats.syntax.traverse._

        movies.traverse(computeAsIO.andThen(ownDebug))
    }

    def showcaseIOofListParallely_v2 = {
        import cats.syntax.parallel._

        movies.parTraverse(computeAsIO.andThen(ownDebug))
    }

    override def run: IO[Unit] = showcaseIOofList_v2.void
}



object IOTraversalExercise extends IOApp.Simple {
    import cats.syntax.apply._
    import cats.instances.list._
    // exercise 1 a
    def sequence[A](listOfIOs: List[IO[A]]): IO[List[A]] = {
        Traverse[List].traverse(listOfIOs)(identity)
    }

    // exercise 1 b generalised wrapper not list
    def sequence_h[F[_]: Traverse, A](wrapperOfIOs: F[IO[A]]): IO[F[A]] = {
        Traverse[F].traverse(wrapperOfIOs)(identity)
    }
    // exercise 1 b with extension method 
    def sequence_h_v2[F[_]: Traverse, A](wrapperOfIOs: F[IO[A]]): IO[F[A]] = {
        import cats.syntax.traverse._
        wrapperOfIOs.traverse(identity)
    }

    // exercise 2 a , convert List of IO to IO of List parallely
    def parSequence[A](listOfIOs: List[IO[A]]): IO[List[A]] = {
        Parallel.parTraverse(listOfIOs)(identity)
    }

    // exercise 2 b with generalised wrapper instead of list
    def parSequence_v2[F[_]: Traverse, A](wrapperOfIOs: F[IO[A]]): IO[F[A]] = {
        Parallel.parTraverse(wrapperOfIOs)(identity)
    }

    // exercise 2 b but with extension methods usage
    def parSequence_v3[F[_]: Traverse, A](wrapperOfIOs: F[IO[A]]): IO[F[A]] = {
        import cats.syntax.parallel._
        wrapperOfIOs.parTraverse(identity)
    }


    // we can do similar things with // SEQUENCE method available

    def sequence_v3[F[_]: Traverse, A](wrapperOfIOs: F[IO[A]]): IO[F[A]] = {
        Traverse[F].sequence(wrapperOfIOs)

        // or we can also do 

        import cats.syntax.traverse._
        wrapperOfIOs.sequence
    }

    // similarly for the parallel versions
    def parSequence_v4[F[_]: Traverse, A](wrapperOfIOs: F[IO[A]]): IO[F[A]] = {
        Parallel.parSequence(wrapperOfIOs)

        // or we can also do 

        import cats.syntax.parallel._
        wrapperOfIOs.parSequence
    }

    override def run: IO[Unit] = ???
}
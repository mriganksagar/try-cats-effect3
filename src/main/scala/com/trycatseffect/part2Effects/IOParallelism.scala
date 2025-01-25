package com.trycatseffect.part2Effects

import cats.effect.IOApp
import cats.effect.IO
import cats.Parallel

object IOParallelism extends IOApp.Simple {

  import com.trycatseffect.utils._

  import cats.syntax.apply._
  
  val meaningOfLife = IO.pure(42)
  val favLanguage = IO.pure("scala")
  
  val goalInLife = (meaningOfLife.bebug, favLanguage.bebug).mapN((num, string) => s"my goal is to learn $string $num times")
  
  // Parallelism in IOs
  // convert a sequential IO to parallel IO
  var parIO1: IO.Par[Int] = Parallel[IO].parallel(meaningOfLife.bebug)
  var parIO2: IO.Par[String] = Parallel[IO].parallel(favLanguage.bebug)
  // import cats.effect.implicits._

  var goalInLifeParallel:IO.Par[String] = (parIO1, parIO2).mapN((num, string) => s"my goal is to learn $string $num times")
  var goalInLife_v2 = Parallel[IO].sequential(goalInLifeParallel)

  import cats.syntax.parallel._
  var goalInLife_v3 = (meaningOfLife.bebug, favLanguage.bebug).parMapN((num, string) => s"my goal is to learn $string $num times")
  override def run: IO[Unit] = goalInLife_v3.bebug.void

}

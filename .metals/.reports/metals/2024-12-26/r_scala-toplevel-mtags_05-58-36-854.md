error id: file://<WORKSPACE>/src/main/scala/com/trycatseffect/part2Effects/IOexercises.scala:[2286..2289) in Input.VirtualFile("file://<WORKSPACE>/src/main/scala/com/trycatseffect/part2Effects/IOexercises.scala", "package com.trycatseffect.part2Effects
import cats.effect.IO

object IOExercises {

    // 1. sequence two IOs and take the result of second one
    def sequenceTakeLast[A, B] (ioa: IO[A], iob: IO[B]): IO[B] = ioa.flatMap(_ => iob)

    def sequenceTakeLastv2[A, B] (ioa: IO[A], iob: IO[B]): IO[B] = ioa *> iob // and then

    def sequenceTakeLastv3[A, B] (ioa: IO[A], iob: IO[B]): IO[B] = ioa >> iob // and then but with by name call
    // 2. sequence two IOs and take the result of second one
    def sequenceTakeFirst[A, B] (ioa: IO[A], iob: IO[B]): IO[A] = ioa.flatMap(a => iob.map(_=> a))

    def sequenceTakeFirstv2[A, B] (ioa: IO[A], iob: IO[B]): IO[A] = ioa <* iob
    // 3. Repeat an IO forever
    def forever[A](io:IO[A]): IO[A] = io.flatMap(_ => forever(io))

    def forever_v2[A](io:IO[A]): IO[A] = io >> forever(io)

    // here in this version eager eval will cause stack overflow due to indefinite evaluation of this IO
    def forever_v3[A](io:IO[A]): IO[A] = io *> forever(io)

    // inbuilt method from cats effect
    def forever_v4[A](io:IO[A]): IO[A] = io.foreverM

    // 4. convert an IO to a different type
    def convert[A, B](ioa: IO[A], value: B): IO[B] = ioa.map(_ => value)

    def convert_v2[A, B](ioa: IO[A], value: B): IO[B] = ioa.as(value)

    // 5. discard value from IO and return unit
    def asUnit[A](io:IO[A]):IO[Unit] = io.map(_=>())

    // discouraged
    def asUnit_v2[A](io:IO[A]):IO[Unit] = io.as(())

    def asUnit_v3[A](io:IO[A]):IO[Unit] = io.void

    // 6. fix stack recursion
    def sum(n:Int): Int = 
        if n<=0 then 0
        else n+ sum(n-1)

    def sumIO(n: Int): IO[Int] = if n <= 0 then IO(0)
        else sumIO(n-1).map(value => value+n)

    // this version uses flatmap as is stack safe due to a concept called trampolining (used in implementation of Cats)
    def sumIO_v1(n:Int): IO[Int] =
        if n <=0 then IO(0)
        else for {
            lastNumber <- IO(n)
            prevSum <- sumIO_v1(n-1)
        } yield prevSum + lastNumber

    // 7. write fibonacci 
    def fibonacci(n:Int): IO[BigInt] = 
        if n <=1 then IO(1)
        else for {
            _ <- IO.unit
            last <- fibonacci(n-1)
            secondlast <- fibonacci(n-2)
        } yield last + secondlast

    def 
    def main(args: Array[String]): Unit = {
        import cats.effect.unsafe.implicits.global
        // println(sumIO(20000).unsafeRunSync())

        val sumIO_90000 = sumIO_v1(90000)
        println(sumIO_90000.unsafeRunSync())
    }
}")
file://<WORKSPACE>/file:<WORKSPACE>/src/main/scala/com/trycatseffect/part2Effects/IOexercises.scala
file://<WORKSPACE>/src/main/scala/com/trycatseffect/part2Effects/IOexercises.scala:66: error: expected identifier; obtained def
    def main(args: Array[String]): Unit = {
    ^
#### Short summary: 

expected identifier; obtained def
error id: file://<WORKSPACE>/src/main/scala/com/trycatseffect/part2Effects/IOexercises.scala:[832..835) in Input.VirtualFile("file://<WORKSPACE>/src/main/scala/com/trycatseffect/part2Effects/IOexercises.scala", "package com.trycatseffect.part2Effects
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

    def 
    // 4. convert an IO to a different type
    def convert[A, B](ioa: IO[A], value: B): IO[B] = ioa.map(_ => value)

    // 5. discard value from IO and return unit
    def asUnit[A](io:IO[A]):IO[Unit] = io.map(_=>())

    // 6. fix stack recursion
    def sumIO(n: Int): IO[Int] = if n <= 0 then IO.(0)
        else sumIO(n-1).map(value => value+n)

    // 7. write fibonacci 
    def fibonacci(n:Int): IO[BigInt]
    def main(args: Array[String]): Unit = {

    }
}")
file://<WORKSPACE>/file:<WORKSPACE>/src/main/scala/com/trycatseffect/part2Effects/IOexercises.scala
file://<WORKSPACE>/src/main/scala/com/trycatseffect/part2Effects/IOexercises.scala:21: error: expected identifier; obtained def
    def convert[A, B](ioa: IO[A], value: B): IO[B] = ioa.map(_ => value)
    ^
#### Short summary: 

expected identifier; obtained def
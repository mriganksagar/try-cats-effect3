file://<WORKSPACE>/src/main/scala/com/trycatseffect/part2Effects/IOexercises.scala
### scala.MatchError: TypeDef(A,TypeBoundsTree(EmptyTree,EmptyTree,EmptyTree)) (of class dotty.tools.dotc.ast.Trees$TypeDef)

occurred in the presentation compiler.

action parameters:
offset: 722
uri: file://<WORKSPACE>/src/main/scala/com/trycatseffect/part2Effects/IOexercises.scala
text:
```scala
package com.trycatseffect.part2Effects
import cats.effect.IO

object IOExercises {

    // 1. sequence two IOs and take the result of second one
    def sequenceTakeLast[A, B] (ioa: IO[A], iob: IO[B]): IO[B] = ioa.flatMap(_ => iob)

    // 2. sequence two IOs and take the result of second one
    def sequenceTakeFirst[A, B] (ioa: IO[A], iob: IO[B]): IO[A] = ioa.flatMap(a => iob.map(_=> a))

    // 3. Repeat an IO forever
    def forever[A](io:IO[A]): IO[A] = io.flatMap(_ => forever(io))

    // 4. convert an IO to a different type
    def convert[A, B](ioa: IO[A], value: B): IO[B] = ioa.map(_ => value)

    // 5. discard value from IO and return unit
    def asUnit[A](io:IO[A]):IO[Unit] = io.map(_=>())

    // 6.@@
    def main(args: Array[String]): Unit = {

    }
}
```



#### Error stacktrace:

```
dotty.tools.pc.completions.KeywordsCompletions$.checkTemplateForNewParents$$anonfun$2(KeywordsCompletions.scala:220)
	scala.Option.map(Option.scala:242)
	dotty.tools.pc.completions.KeywordsCompletions$.checkTemplateForNewParents(KeywordsCompletions.scala:221)
	dotty.tools.pc.completions.KeywordsCompletions$.contribute(KeywordsCompletions.scala:46)
	dotty.tools.pc.completions.Completions.completions(Completions.scala:119)
	dotty.tools.pc.completions.CompletionProvider.completions(CompletionProvider.scala:87)
	dotty.tools.pc.ScalaPresentationCompiler.complete$$anonfun$1(ScalaPresentationCompiler.scala:143)
```
#### Short summary: 

scala.MatchError: TypeDef(A,TypeBoundsTree(EmptyTree,EmptyTree,EmptyTree)) (of class dotty.tools.dotc.ast.Trees$TypeDef)
file://<WORKSPACE>/src/main/scala/com/trycatseffect/part2Effects/efffectsExercises.scala
### scala.MatchError: TypeDef(A,TypeBoundsTree(EmptyTree,EmptyTree,EmptyTree)) (of class dotty.tools.dotc.ast.Trees$TypeDef)

occurred in the presentation compiler.

action parameters:
offset: 829
uri: file://<WORKSPACE>/src/main/scala/com/trycatseffect/part2Effects/efffectsExercises.scala
text:
```scala
package com.trycatseffect.part2Effects

case class MyIO[A](unsafeRun: () => A) {
  def map[B](f: A => B): MyIO[B] = {
    MyIO(() => f(unsafeRun()))
  }

  def flatMap[B](f: A => MyIO[B]): MyIO[B] = {
    MyIO(() => f(unsafeRun()).unsafeRun())
  }
}

object EffectsExercise {
  // 1. An IO which returns the current time of the system
  val currentTimeIO = MyIO { () => System.currentTimeMillis() }

  // 2. An IO which measures the duration of a computation
  def measure[A](computation: MyIO[A]): MyIO[Long] = MyIO { () =>
    {
      val timebefore = currentTimeIO.unsafeRun()
      computation.unsafeRun()
      val timeafter = currentTimeIO.unsafeRun()
      timeafter - timebefore
    }
  }

  // 3. An IO that prints something to the console
  def printIO(value: String): MyIO[Unit] = MyIO{() => println(value)}
  
  // 4.@@
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
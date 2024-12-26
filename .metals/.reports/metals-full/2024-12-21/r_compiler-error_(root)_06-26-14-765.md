file://<WORKSPACE>/src/main/scala/com/trycatseffect/tryScala3/typeclasses.scala
### java.lang.StringIndexOutOfBoundsException: Range [188, 210) out of bounds for length 207

occurred in the presentation compiler.

action parameters:
offset: 194
uri: file://<WORKSPACE>/src/main/scala/com/trycatseffect/tryScala3/typeclasses.scala
text:
```scala
package com.trycatseffect.tryScala3

object TryTypeClasses{

    trait JSONSerialiser[T]{
        def toJson(value:T): String
    }

    case class People(name:String, age:Int)

    given People@@Serialiser
}

```



#### Error stacktrace:

```
java.base/jdk.internal.util.Preconditions$1.apply(Preconditions.java:55)
	java.base/jdk.internal.util.Preconditions$1.apply(Preconditions.java:52)
	java.base/jdk.internal.util.Preconditions$4.apply(Preconditions.java:213)
	java.base/jdk.internal.util.Preconditions$4.apply(Preconditions.java:210)
	java.base/jdk.internal.util.Preconditions.outOfBounds(Preconditions.java:98)
	java.base/jdk.internal.util.Preconditions.outOfBoundsCheckFromToIndex(Preconditions.java:112)
	java.base/jdk.internal.util.Preconditions.checkFromToIndex(Preconditions.java:349)
	java.base/java.lang.String.checkBoundsBeginEnd(String.java:4865)
	java.base/java.lang.String.substring(String.java:2834)
	dotty.tools.pc.PcCollector.isGeneratedGiven(PcCollector.scala:133)
	dotty.tools.pc.PcCollector.soughtSymbols(PcCollector.scala:209)
	dotty.tools.pc.PcCollector.resultWithSought(PcCollector.scala:345)
	dotty.tools.pc.PcCollector.result(PcCollector.scala:335)
	dotty.tools.pc.PcDocumentHighlightProvider.highlights(PcDocumentHighlightProvider.scala:33)
	dotty.tools.pc.ScalaPresentationCompiler.documentHighlight$$anonfun$1(ScalaPresentationCompiler.scala:175)
```
#### Short summary: 

java.lang.StringIndexOutOfBoundsException: Range [188, 210) out of bounds for length 207
file://<WORKSPACE>/src/main/scala/com/trycatseffect/tryScala3/typeclasses.scala
### dotty.tools.dotc.ast.Trees$UnAssignedTypeException: type of TypeTree is not assigned

occurred in the presentation compiler.

action parameters:
uri: file://<WORKSPACE>/src/main/scala/com/trycatseffect/tryScala3/typeclasses.scala
text:
```scala
package com.trycatseffect.tryScala3

object TryTypeClasses{

    trait JSONSerialiser[T]{
        def toJson(value:T): String
    }

    case class Person(name:String, age:Int)

    given PeopleJSONSerialiser: JSONSerialiser[Person] with {
        override def toJson(value: Person): String = s"name: ${value.name}, and ${value.age} years old"
    }

    given ListSerialiser[T](using JSONSerialiser[T]): JSONSerialiser[List[T]] with {
        override def toJson(value: List[T]): String = value.map(_.toJson).mkString("[", ", ","]")
    } 


    extension[T](value:T)(using serialiser:JSONSerialiser[T]){
        def toJson: String = serialiser.toJson(value) 
    }

    def main(args: Array[String]): Unit = {
        val people = List(Person("alok", 40), Person("aditya", 35))
        val suraj = Person("suraj", 37)
        println(suraj.toJson)
        println(people.toJson)
    }
}


object TryTypeClasses{

    trait JSONSerialiser[T]{
        def toJson(value:T): String
    }

    case class Person(name:String, age:Int)

    given PeopleJSONSerialiser: JSONSerialiser[Person] with {
        override def toJson(value: Person): String = s"name: ${value.name}, and ${value.age} years old"
    }

    given ListSerialiser[T](using JSONSerialiser[T]): JSONSerialiser[List[T]] with {
        override def toJson(value: List[T]): String = value.map(_.toJson).mkString("[", ", ","]")
    } 


    extension[T](value:T)(using serialiser:JSONSerialiser[T]){
        def toJson: String = serialiser.toJson(value) 
    }

    def main(args: Array[String]): Unit = {
        val people = List(Person("alok", 40), Person("aditya", 35))
        val suraj = Person("suraj", 37)
        println(suraj.toJson)
        println(people.toJson)
    }
}
```



#### Error stacktrace:

```
dotty.tools.dotc.ast.Trees$Tree.tpe(Trees.scala:74)
	dotty.tools.pc.InferredType$.unapply(PcSyntheticDecorationProvider.scala:236)
	dotty.tools.pc.PcSyntheticDecorationsProvider.collectDecorations(PcSyntheticDecorationProvider.scala:95)
	dotty.tools.pc.PcSyntheticDecorationsProvider.$anonfun$1(PcSyntheticDecorationProvider.scala:48)
	dotty.tools.dotc.ast.Trees$Instance$DeepFolder.apply(Trees.scala:1802)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.foldOver(Trees.scala:1755)
	dotty.tools.dotc.ast.Trees$Instance$DeepFolder.apply(Trees.scala:1802)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.foldOver(Trees.scala:1752)
	dotty.tools.dotc.ast.Trees$Instance$DeepFolder.apply(Trees.scala:1802)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.fold$1(Trees.scala:1660)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.apply(Trees.scala:1662)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.foldOver(Trees.scala:1755)
	dotty.tools.dotc.ast.Trees$Instance$DeepFolder.apply(Trees.scala:1802)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.foldOver(Trees.scala:1752)
	dotty.tools.dotc.ast.Trees$Instance$DeepFolder.apply(Trees.scala:1802)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.fold$1(Trees.scala:1660)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.apply(Trees.scala:1662)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.foldOver(Trees.scala:1761)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.foldOver(Trees.scala:1666)
	dotty.tools.dotc.ast.Trees$Instance$DeepFolder.apply(Trees.scala:1802)
	dotty.tools.pc.PcSyntheticDecorationsProvider.provide(PcSyntheticDecorationProvider.scala:49)
	dotty.tools.pc.ScalaPresentationCompiler.syntheticDecorations$$anonfun$1(ScalaPresentationCompiler.scala:117)
```
#### Short summary: 

dotty.tools.dotc.ast.Trees$UnAssignedTypeException: type of TypeTree is not assigned
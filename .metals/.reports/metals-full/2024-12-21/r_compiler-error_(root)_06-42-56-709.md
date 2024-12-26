file://<WORKSPACE>/src/main/scala/com/trycatseffect/tryScala3/typeclasses.scala
### scala.MatchError: TypeDef(T,TypeBoundsTree(EmptyTree,EmptyTree,EmptyTree)) (of class dotty.tools.dotc.ast.Trees$TypeDef)

occurred in the presentation compiler.

action parameters:
offset: 430
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

    given ListSerialiser[T](using JSONSerialiser[T]): JSONSerialiser[List[T]] w@@ 


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
dotty.tools.pc.completions.KeywordsCompletions$.checkTemplateForNewParents$$anonfun$2(KeywordsCompletions.scala:220)
	scala.Option.map(Option.scala:242)
	dotty.tools.pc.completions.KeywordsCompletions$.checkTemplateForNewParents(KeywordsCompletions.scala:221)
	dotty.tools.pc.completions.KeywordsCompletions$.contribute(KeywordsCompletions.scala:46)
	dotty.tools.pc.completions.Completions.completions(Completions.scala:119)
	dotty.tools.pc.completions.CompletionProvider.completions(CompletionProvider.scala:87)
	dotty.tools.pc.ScalaPresentationCompiler.complete$$anonfun$1(ScalaPresentationCompiler.scala:143)
```
#### Short summary: 

scala.MatchError: TypeDef(T,TypeBoundsTree(EmptyTree,EmptyTree,EmptyTree)) (of class dotty.tools.dotc.ast.Trees$TypeDef)
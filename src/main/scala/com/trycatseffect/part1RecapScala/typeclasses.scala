package com.trycatseffect.part1RecapScala

object TryTypeClasses{

    case class Person(name:String, age:Int)
    
    trait JSONSerialiser[T]{
        def toJson(value:T): String
    }

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


object TryTypeClasses2{

    case class Person(name:String, age:Int)

    trait JSONSerialiser[T]{
        extension(value: T){
            def toJson: String
        }
    }

    given PeopleJSONSerialiser: JSONSerialiser[Person] with {
        extension(value: Person)
            def toJson  = s"name: ${value.name}, and ${value.age} years old"
    }

    given ListSerialiser[T](using JSONSerialiser[T]): JSONSerialiser[List[T]] with {
        extension(value: List[T])
            def toJson: String = value.map(_.toJson).mkString("[", ", ","]")
    } 

    def main(args: Array[String]): Unit = {
        val people = List(Person("alok", 40), Person("aditya", 35))
        val suraj = Person("suraj", 37)
        println(suraj.toJson)
        println(people.toJson)
    }
}
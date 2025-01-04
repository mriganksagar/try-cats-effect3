package com.trycats

import cats.Semigroupal

// while semigroup allows us to join values, semigroupal allows us to join contexts

object SemigroupalTryout {
    
    def semigroupalproduct:Unit = {
        // if both are Some then it evaluates to some of tuple else it is None if any is none
        val v = Semigroupal[Option].product(Some(12), Some(213))
        val x = Semigroupal[Option].product(Some(123), None)
        val y = Semigroupal[Option].product(None, Some(123))
        val z = Semigroupal[Option].product(None, None)
        println(v)
        println(x)
        println(y)
        println(z)
    }

    def semigroupaltuple: Unit = {
        val abc = Semigroupal.tuple4(Option(true), Option("bababubu"), Some("atti"), Some(45))
        println(abc)

        val abcmap = Semigroupal.map3(Option(45), Option(41), Some(31))(_+_+_)
        println(abcmap)
        val abcmapFailed = Semigroupal.map4(Option(41), Some("aloo"), Some(42), None)(_+_+_+_)
        println(abcmapFailed)
    }


    def shorthandApplySyntax: Unit = {

        import cats.syntax.apply._

        // one thing to notice is this wont compile below but if i change first Some to Option it will compile
        // cause method is available for context Option not Some 
        // val tupledOption = (Some(3), Some(5), Some(32)).tupled
        val tupledValue1 = (Option(3), Some(5), Some(32)).tupled
        println(tupledValue1)
        // this will also compile due to option being lowest common ancestor class of some and none and method available for option context
        val tupledValue2 = (Some(3), Some(5), Some(32), None).tupled
        println(tupledValue2)

        // MapN methods
        val mapValue1 = (Option(3), Some(4), Option(42)).mapN((a, b, c) => a*b+c)
        println(mapValue1)
    }

    def fancyFunctorAndApplySyntax: Unit = {
        import cats.Monoid
        import cats.instances.int._     // for int monoid instance
        import cats.instances.string._  // for string monoid instance
        import cats.instances.list._    // for list monoid instance
        import cats.syntax.apply._      // for imapN method
        import cats.syntax.semigroup._  // for |+| method
        // WHAT IF: We want to create a monoid for a case class 

        case class Catty(
            name: String, 
            age: Int,
            favouriteFood: List[String]
        )
     
        given cattyMonoid: Monoid[Catty] = (
            Monoid[String],
            Monoid[Int],
            Monoid[List[String]]
        ).imapN(Catty.apply)(c => (c.name, c.age, c.favouriteFood))

        val catty1 = Catty("alok", 40, List("chicken", "mutton"))
        val catty2 = Catty("aditya", 35, List("fish", "mutton"))

        val catty3 = catty1 |+| catty2

        println(catty3)

    }

    // Semigroupal Applied to Monads

    def semigroupalMonads: Unit = {
        import cats.instances.list._   // for implicit list semigroup instance
        import cats.instances.either._ // for implicit either semigroup instance
        
        
        // interestingly this will give off a cartesian product
        val list_semigrouped = Semigroupal[List].product(List(1,2), List('a', 'b', 'c'))
        println(list_semigrouped)

        // here println will print Left("error 1") as it is the first error 
        // and it will not evaluate the second error working as a fail fast mechanism
        val either_semigrouped = Semigroupal[Either[String, *]].product(Left("error 1"), Left("error 2"))
        println(either_semigrouped)

    }


    def main(args: Array[String]): Unit = {
        // semigroupalproduct
        // semigroupaltuple
        // shorthandApplySyntax
        // fancyFunctorAndApplySyntax
        semigroupalMonads
    }   
}
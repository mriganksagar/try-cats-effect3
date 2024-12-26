error id: file://<WORKSPACE>/src/main/scala/com/trycatseffect/part2Effects/efffectsExercises.scala:[507..510) in Input.VirtualFile("file://<WORKSPACE>/src/main/scala/com/trycatseffect/part2Effects/efffectsExercises.scala", "package com.trycatseffect.part2Effects


case class  MyIO[A](unsafeRun: () => A) {
    def map[B](f:A => B): MyIO[B] = {
        MyIO(() => f(unsafeRun()))
    }
    
    def flatMap[B](f: A => MyIO[B]): MyIO[B] = {
        MyIO(() => f(unsafeRun()).unsafeRun())
    }
} 


object EffectsExercise {
    // 1. An IO which returns the current time of the system




    val currentTimeIO = MyIO{()=> System.currentTimeMillis()}
    
    // 2. An IO which measures the duration of a computation


    def 
    for {
        beforeTime <- currentTime
        result <- 
    }
}")
file://<WORKSPACE>/file:<WORKSPACE>/src/main/scala/com/trycatseffect/part2Effects/efffectsExercises.scala
file://<WORKSPACE>/src/main/scala/com/trycatseffect/part2Effects/efffectsExercises.scala:27: error: expected identifier; obtained for
    for {
    ^
#### Short summary: 

expected identifier; obtained for
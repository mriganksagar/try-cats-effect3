package com.trycatseffect.part4coordination

import cats.effect.{IO, IOApp, Deferred, Ref}
import scala.collection.immutable.Queue
import scala.concurrent.duration.*
import scala.util.Random
import com.trycatseffect.utils.ownDebug
import cats.effect.kernel.Outcome.Succeeded
import cats.effect.kernel.Outcome.Errored
import cats.effect.kernel.Outcome.Canceled

abstract class Mutex {
    def acquire: IO[Unit]
    def release: IO[Unit]
}

object Mutex {
    case class State(locked: Boolean, waiting: Queue[Deferred[IO, Unit]])
    val initialState = State(false, Queue.empty)

    def createMutexWithCancellation(state: Ref[IO, State]): Mutex = new Mutex {
        
        /*  Rockthejvm used state.modify and returned release here 
            though i think release is not required and can cause problems
            if the calling thread is at signal.get it means it hasn't yet acquired lock,
            so removing signal from queue will do the job
            
            AND if we release, it might release the lock when some thread has already acquired it
            and two thread might go concurrently into the critical section 
         */
        override def acquire: IO[Unit] = IO.uncancelable{ poll =>
            for {
                signal <- IO.deferred[Unit]
                _ <- {
                    val cleanup: IO[Unit] = state.update{
                        case State(isLocked, queue) => State(isLocked, queue.filterNot(_ eq signal))
                    }

                    state.flatModify {
                        case State(false, queue) => State(true, queue) -> IO.unit // syntax for pair
                        case State(true, queue)  => State(true, queue.enqueue(signal)) -> poll(signal.get).onCancel(cleanup)
                    }
                }
            } yield ()
        }
        /* 
            as state modifiers are already atomic no need to wrap it inside uncancellable
         */
        override def release: IO[Unit] = state.flatModify {
            case State(false, _) => initialState -> IO.unit
            case State(true, queue) =>
                if queue.isEmpty then initialState -> IO.unit
                else {
                    val (signal, newQueue) = queue.dequeue
                    State(true, newQueue) -> signal.complete(()).uncancelable.void // i marked it uncancellable to be on safer side
                }
        }
    }

    def createSimpleMutex(): IO[Mutex] = IO.ref(initialState).map { state =>
        new Mutex {
            override def acquire: IO[Unit] = for {
                signal <- IO.deferred[Unit]
                _ <- state.flatModify {
                    case State(false, queue) => State(true, queue) -> IO.unit // syntax for pair
                    case State(true, queue)  => State(true, queue.enqueue(signal)) -> signal.get
                }
            } yield ()

            override def release: IO[Unit] = state.flatModify {
                case State(false, _) => initialState -> IO.unit
                case State(true, queue) =>
                    if queue.isEmpty then initialState -> IO.unit
                    else {
                        val (signal, newQueue) = queue.dequeue
                        State(true, newQueue) -> signal.complete(()).void
                    }
            }
        }
    }

    def apply() = IO.ref(initialState).map(createMutexWithCancellation)
}

import cats.syntax.traverse._
object MutexPlayground extends IOApp.Simple {

    def criticalTask: IO[Int] = IO.sleep(1.second) >> IO(Random.nextInt(100))

    /* 
        a method that call critical task but use mutex locks around it

        in ROCKTHEJVM version he didn't write
        guarantee(mutex.release) in critical task but released mutex in line after it

        in my opinion it is highly important so a deadlock doesn't occur
     */
    def lockingTask(id: Int, mutex: Mutex): IO[Int] =
        for {
            _ <- IO(s"[task $id] starting ...").ownDebug
            _ <- IO(s"[task $id] waiting to acquire lock").ownDebug
            _ <- mutex.acquire
            // critical section
            res <- criticalTask.guarantee(mutex.release)
            _ <- IO(s"[task $id] releasing lock").ownDebug
            _ <- IO(s"[task $id] got result $res").ownDebug
        } yield res

    def demoLockingTasks: IO[List[Int]] = for {
        mutex <- Mutex.createSimpleMutex()
        result <- (1 to 10).toList.traverse(lockingTask(_, mutex))
    } yield result

    def cancelableLockingTask(id: Int, mutex: Mutex): IO[Int] =
        if id%2 == 0 then lockingTask(id, mutex)
        else for {
            fib <- lockingTask(id, mutex).onCancel(IO(s"[Task Id: $id] cancelling task").ownDebug.void).start
            _ <- IO.sleep(1.second) >> fib.cancel
            out <- fib.join
            result <- out match {
                case Succeeded(fa) => fa
                case Errored(_) => IO(-1)
                case Canceled() => IO(-2)
            }
        } yield result

    def demoCancelableLockingTasks: IO[List[Int]] = for {
        mutex <- Mutex()
        result <- (1 to 10).toList.traverse(cancelableLockingTask(_, mutex))
    } yield result
    override def run: IO[Unit] = demoCancelableLockingTasks.map(println).void

}

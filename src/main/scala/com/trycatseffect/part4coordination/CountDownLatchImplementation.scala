package com.trycatseffect.part4coordination

import cats.effect.{IO, IOApp}
import cats.effect.kernel.Ref
import cats.effect.kernel.Deferred
import scala.collection.immutable.Queue
import cats.syntax.parallel._

trait MyCtLatch {
    def await: IO[Unit]
    def release: IO[Unit]
}

object MyCtLatch {

    case class State(counter: Int, queue: Queue[Deferred[IO, Unit]])

    def createMyCtLatch(state: Ref[IO, State]): MyCtLatch = new MyCtLatch {

        def await: IO[Unit] = IO.uncancelable { poll =>
            for {
                signal <- IO.deferred[Unit]
                _ <- state.flatModify {
                    case s @ State(c, q) if c <= 0 => s -> IO.unit
                    case State(c, q) =>
                        State(c, q.enqueue(signal)) -> poll(signal.get).onCancel {
                            state.update { case State(c, q) => State(c, q.filterNot(_ == signal)) }
                        }
                }
            } yield ()
        }

        def release: IO[Unit] =
            for {
                State(newc, q) <- state.updateAndGet { case s @ State(c, q) =>
                    if c > 0 then State(c - 1, q) else s
                }
                _ <- if newc == 0 then q.parTraverse(_.complete(())) else IO.unit
            } yield ()
    }

    def apply(n: Int): IO[MyCtLatch] = for {
        state <- IO.ref(State(n, Queue.empty))
        latch <- IO(createMyCtLatch(state))
    } yield latch
}

/* 
    the above implementation can be simplified due to the fact 
    we don't need multiple deferred but only one
    in case of mutex every consumer should have their own signal
    here only one signal is needed
 */

object CTLatch {
    sealed trait State
    case object Done extends State
    case class Live(count: Int, signal: Deferred[IO, Unit]) extends State

    def create(state: Ref[IO, State]): MyCtLatch = new MyCtLatch {
        def await: IO[Unit] = state.get.flatMap{ 
            case Done => IO.unit
            case Live(_, signal) => signal.get
        }

        // release should not be cancellable, else it blocks awaiting fibers indefinitely
        def release: IO[Unit] = state.flatModify{
            case Done => Done -> IO.unit
            case Live(1, signal) => Done -> signal.complete(()).void
            case Live(n, signal) => Live(n-1, signal) -> IO.unit 
        }.uncancelable
    }

    def apply(n: Int): IO[MyCtLatch] = for {
        signal <- IO.deferred[Unit]
        state <- IO.ref[State](Live(n, signal))
    } yield create(state)
}

object CountDownLatchImplementationDemo extends IOApp.Simple {

    override def run: IO[Unit] = ???
}

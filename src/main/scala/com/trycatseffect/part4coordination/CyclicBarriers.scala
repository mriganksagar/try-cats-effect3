package com.trycatseffect.part4coordination

import cats.effect.IOApp
import cats.effect.IO
import com.trycatseffect.utils.bebug
import scala.util.Random
import scala.concurrent.duration._
import cats.effect.std.CyclicBarrier
import cats.syntax.parallel._
import cats.effect.kernel.Deferred
import cats.effect.kernel.Ref

trait CBarrier {
    def await: IO[Unit]
}

object CBarrier {

    case class State(waiting: Int, signal: Deferred[IO, Unit])

    private def create(threshold: Int, state: Ref[IO, State]) = new CBarrier {
        def await: IO[Unit] = for {
            newSignal <- IO.deferred[Unit]
            _ <- state.flatModify {
                case State(waiting, signal) if waiting == threshold - 1 =>
                    State(0, newSignal) -> signal.complete(())
                case State(waiting, signal) => State(waiting + 1, signal) -> signal.get
            }
        } yield ()
    }

    def apply(threshold: Int): IO[CBarrier] = for {
        firstSignal <- IO.deferred[Unit]
        state <- IO.ref(State(0, firstSignal))
    } yield create(threshold, state)
}

object CyclicBarriersDemo extends IOApp.Simple {

    /*
        Cyclic barriers block observers untill there are n observers and again do the same for next observers

        lets create an example for a restaurant buffet
        a buffet only opens for a cyclic batch of n users,

        // using my own implementation also works well
     */

    def buffetCustomer(id: Int, barrier: CyclicBarrier[IO]): IO[Unit] = for {
        _ <- IO(s"[User $id]: Registering for a buffet").bebug
        _ <- IO.sleep(Random.nextDouble.second)
        _ <- IO(s"[User $id]: Done Registering, waiting outside").bebug
        _ <- barrier.await
        _ <- IO(s"[User $id]: the food is really tasty").bebug
    } yield ()

    def restaurantSimulation: IO[Unit] = for {
        _ <- IO(s"The restaurant is up for buffet").bebug
        barrier <- CyclicBarrier[IO](8)
        _ <- (1 to 15).toList.parTraverse(buffetCustomer(_, barrier))
    } yield ()
    override def run: IO[Unit] = restaurantSimulation
}

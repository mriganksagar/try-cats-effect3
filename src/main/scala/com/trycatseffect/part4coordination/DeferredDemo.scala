package com.trycatseffect.part4coordination

import cats.effect.{IO, IOApp, Ref, Deferred}
import com.trycatseffect.utils.ownDebug
import scala.concurrent.duration.*

object DeferredDemo extends IOApp.Simple {

    // creates an IO that gives a deffered value
    val aDeferred: IO[Deferred[IO, Int]] = Deferred[IO, Int]
    val aDeferred_v2: IO[Deferred[IO, Int]] = IO.deferred[Int]

    /*
        Get Method
        blocks the calling fiber (semantically) until some other fiber completes the deferred value
     */

    val reader: IO[Int] = aDeferred.flatMap { deferred =>
        deferred.get // blocks this thread or runtime semantically (not blocking actual thread)
    }

    // set method to set the value

    val write: IO[Boolean] = aDeferred.flatMap { signal =>
        signal.complete(42)
    }

    // Exercise... Give a demo of Defer through a producer and consumer pattern

    def demoDefersProducerConsumer(): IO[Unit] = {

        def consumer(signal: Deferred[IO, Int]) = for {
            _ <- IO(s"[Consumer]: Waiting for the result").ownDebug
            value <- signal.get
            _ <- IO(s"[Consumer]: got the result $value").ownDebug
        } yield ()

        def producer(signal: Deferred[IO, Int]): IO[Unit] = for {
            _ <- IO(s"[Producer]: computing result").ownDebug
            _ <- IO.sleep(2.second)
            value <- signal.complete(42)
            _ <- IO(s"[Producer]: sent the result $value").ownDebug
        } yield ()

        for {
            signal <- IO.deferred[Int]
            fibC <- consumer(signal).start
            fibP <- producer(signal).start
            _ <- fibC.join
            _ <- fibP.join
        } yield ()
    }

    /*
        Exercise ... Simulate downloading some content

         - a payload i.e a list of strings
         - a downloading mechanism
         - a notifying mechanism

        first we demo without deferred mechanism
     */

    val payload = List(
      "chand sifarish",
      "jo karta hamari",
      "deta voh rumko bata",
      "sharm-o-haya ke parde girake",
      "karni hai humko khata <EOL>"
    )

    // necessary imports for parts below
    import cats.syntax.traverse._

    def fileNotifierWithRef(): IO[Unit] = {

        def downloader(contentRef: Ref[IO, String]) = payload.traverse { data =>
            IO.sleep(1.second) >> IO(s"[Downloader] downloaded $data").ownDebug
                >> contentRef.update(_ + data)
        }.void

        def notifier(contentRef: Ref[IO, String]): IO[Unit] =
            for {
                part <- contentRef.get
                _ <-
                    if part.endsWith("<EOL>") then IO(s"[Notifier] downloading completed").ownDebug
                    else
                        IO(s"[Notifier] downloading...").ownDebug >> IO.sleep(
                          0.5.second
                        ) >> notifier(contentRef)
            } yield ()

        for {
            contentRef <- IO.ref("")
            fibDownloader <- downloader(contentRef).start
            fibNotifier <- notifier(contentRef).start
            _ <- fibDownloader.join
            _ <- fibNotifier.join
        } yield ()
    }

    /*
        The problem with above code is
        that notifier keeps checking if file is downloading or not every half second
        By using defer we can make it event driven
     */

    def fileNotifierWithDeferred(): IO[Unit] = {

        def notifier(signal: Deferred[IO, Boolean]): IO[Unit] =
            for {
                _ <- IO(s"[Notifier]: Downloading file...").ownDebug
                isCompleted <- signal.get
                _ <- if isCompleted then IO(s"[Notifier]: Downloaded file successfuly").ownDebug else IO(s"[Notifier]: Download failed").ownDebug
            } yield ()


        def downloadChunk(chunk: String, contentRef: Ref[IO, String], signal: Deferred[IO, Boolean]): IO[Unit] = 
            for {
                _ <- IO.sleep(1.second)
                _ <- IO(s"[Downloader]: downloader recieved $chunk").ownDebug
                latest_content <- contentRef.updateAndGet(_+chunk)
                _ <- if chunk.contains("<EOL>") then signal.complete(true) else IO.unit
            } yield ()

        for {
            contentRef <- IO.ref("")
            signal <- IO.deferred[Boolean]
            fibNotifier <- notifier(signal).start
            fibDownloader <- payload.traverse(chunk => downloadChunk(chunk, contentRef, signal)).start
            _ <- fibNotifier.join
            _ <- fibDownloader.join
        } yield ()
    }
    override def run: IO[Unit] = fileNotifierWithDeferred()
}

package com.trycatseffect.part4coordination

import cats.effect.IOApp
import cats.effect.IO
import cats.effect.std.CountDownLatch
import com.trycatseffect.utils.bebug
import cats.syntax.parallel._ // for par Traverse
import cats.syntax.traverse._ // for traverse (sequential)
import scala.concurrent.duration._
import cats.effect.kernel.Resource
import java.io.FileWriter
import scala.io.Source
import scala.util.Random

object CountdownLatches extends IOApp.Simple {
    private val projectDir = sys.props("user.dir")
    
    def triggerCountdown(n: Int, latch: CountDownLatch[IO]): IO[String] =
        if n <= 0 then IO(s"Go").bebug
        else
            IO(s"$n ...").bebug >> IO.sleep(0.5.second) >> latch.release >> triggerCountdown(
              n - 1,
              latch
            )

    def createOneRunner(id: Int, latch: CountDownLatch[IO]): IO[String] =
        IO(s"waiting for signal").bebug >>
            latch.await >>
            IO(s"[Runner:id], running").bebug

    def sprintingProgram = for {
        latch <- CountDownLatch[IO](5)
        _ <- IO(s"Starting the race").bebug
        fib_trigger <- triggerCountdown(5, latch).start
        _ <- (1 to 10).toList.parTraverse(createOneRunner(_, latch))
        _ <- fib_trigger.join
    } yield ()

    /*
        Exercise simulate a file downloader on multiple threads
     */

    object FileServer {
        val fileChunksList = Array(
          "aankho me base ho tum",
          "tumhe dil me chupa lunga",
          "jab chahe tumhe dekhu",
          "aaina bana lunga",
          "heyyy he he hooo ho ho"
        )

        def getNumChunks: IO[Int] = IO(fileChunksList.length)
        def getFileChunk(i: Int): IO[String] = IO(fileChunksList(i))
    }

    def writeToFile(path: String, content: String): IO[Unit] = {
        val fileResource = Resource.make(IO(FileWriter(path)))(writer => IO(writer.close()))
        fileResource.use(writer => IO(writer.write(content)))
    }

    // this read file content from one file and write in another
    def appendFileContent(fromPath: String, toPath: String) = {
        val compositeResource = for {
            reader <- Resource.make(IO(Source.fromFile(fromPath)))(w => IO(w.close))
            writer <- Resource.make(IO(FileWriter(toPath, true)))(w => IO(w.close))
        } yield (reader, writer)

        compositeResource.use { case (reader, writer) =>
            IO(reader.getLines.foreach(writer.write))
        }
    }

    /*
        call file server api get number of chunks
        start a countdown latch
        and n fibers to download
        block on latch until each task has finished
        after all chunks are done stitch all files together (using appendFileContent)
     */

    def downloadChunk(chunkId: Int, latch: CountDownLatch[IO], downloadPath: String): IO[Unit] =
        for {
            _ <- IO.sleep(scala.util.Random.nextInt(1000).milli)
            data <- FileServer.getFileChunk(chunkId)
            _ <- writeToFile(downloadPath, data)
            _ <- latch.release
        } yield ()

    def downloadFile(path: String): IO[Unit] = for {
        n <- FileServer.getNumChunks
        latch <- CountDownLatch[IO](n)
        _ <- (0 until n).toList.parTraverse(i => downloadChunk(i, latch, path + i))
        _ <- latch.await
        _ <- (0 until n).toList.traverse(i => appendFileContent(path+i, path)) 
    } yield ()

    def downloadFile(fileName: String, destFolder: String): IO[Unit] = downloadFile(s"$destFolder/$fileName")

    override def run: IO[Unit] = downloadFile("lyrics-ankhon-me.txt", s"$projectDir/src/content")

}

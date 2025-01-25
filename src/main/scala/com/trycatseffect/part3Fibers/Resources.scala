package com.trycatseffect.part3Fibers

import cats.effect.IO
import cats.effect.IOApp
import com.trycatseffect.utils.bebug

import java.io.{File, FileReader}
import java.util.Scanner
import scala.concurrent.duration.*
import com.trycatseffect.part3Fibers.BracketsExercise.openFileScanner
import com.trycatseffect.part3Fibers.BracketsPattern.Connection
import cats.effect.kernel.Resource
import cats.effect.kernel.Outcome.{ Succeeded, Canceled, Errored }

object BracketsPattern extends IOApp.Simple {

    // use case manage a connection lifecycle

    class Connection(url: String) {
        def open: IO[String] = IO(s"opening connection to $url").bebug
        def close: IO[String] = IO(s"closing connection to $url").bebug
    }

    val asyncFetchUrl = for {
        fib <- (new Connection("fakeer.com").open *> IO.sleep(
          Int.MaxValue.second
        )).start
        _ <- IO.sleep(1.second) *> fib.cancel
    } yield ()

    // the problem in above method is that it is leaky, it creates/ open a connection but never closes it
    // lets solve it using on cancel callback

    val correctAsyncFetchUrl = for {
        conn <- IO(new Connection("fakeers.com"))
        fib <- (conn.open *> IO.sleep(1.second)).onCancel(conn.close.void).start
        _ <- IO.sleep(1.second) *> fib.cancel
    } yield ()

    // this pattern is common and called bracket pattern

    val bracketFetchUrl = IO(new Connection("fakeer.com")).bracket(conn =>
        conn.open *> IO.sleep(Int.MaxValue.second)
    )(conn => conn.close.void)

    val bracketProgram = for {
        fib <- bracketFetchUrl.start
        _ <- IO.sleep(1.second) *> fib.cancel
    } yield ()

    override def run: IO[Unit] = bracketProgram
}

object BracketsExercise extends IOApp.Simple {

    // Exercise: Read the file with the bracket pattern
    // open a scanner
    // read file line by line every 100 milli seconds (if needed add sleep)
    // close the scanner (if success or cancelled both)
    def openFileScanner(path: String): IO[Scanner] =
        IO(
          new Scanner(
            new FileReader(
              new File(path.replaceFirst("^~", System.getProperty("user.home")))
            )
          )
        )

    def bracketReadFile(path: String): IO[Unit] = {
        IO(s"opening file at $path") >>
            openFileScanner(path).bracket { scanner =>
                def readLineByLine: IO[Unit] =
                    if scanner.hasNextLine() then
                        IO(scanner.nextLine()).bebug >> IO.sleep(
                          100.milli
                        ) >> readLineByLine
                    else IO.unit
                readLineByLine
            } { scanner => IO(s"closing file at $path") >> IO(scanner.close()) }
    }
    override def run: IO[Unit] = bracketReadFile("~/code/sheeiit.txt")
}

object Resources extends IOApp.Simple {

    // Resources

    // nesting brackets become complicated
    def connFromConfig(path: String): IO[Unit] = {
        openFileScanner(path).bracket { scanner =>
            // acquire a connection based on the content from file
            IO(new Connection(scanner.nextLine())).bracket { conn =>
                conn.open >> IO.never
            }(conn => conn.close.void)
        }(scanner => IO("closing file").bebug >> IO(scanner.close()))
    }

    // there is a better abstraction "Resource" over brackets pattern
    val connectionResource =
        Resource.make(IO(new Connection("aloo.com")))(conn => conn.close.void)

    val resourceFetchUrl = for {
        fib <- connectionResource.use(conn => conn.open >> IO.never).start
        _ <- IO.sleep(1.second) >> fib.cancel
    } yield ()

    // Exercise: Read a text file with one line every 100 millis using resource

    def resourceReadFile(path: String): IO[Unit] = {
        val resourceFile = Resource.make(openFileScanner(path))(scanner =>
            IO(s"closing file at $path").bebug *> IO(scanner.close())
        )

        IO(s"opening file at $path").bebug >> resourceFile.use { scanner =>
            def readLine: IO[Unit] = if scanner.hasNextLine() then
                IO(scanner.nextLine()).bebug >> IO.sleep(
                  100.milli
                ) >> readLine
            else IO.unit
            readLine
        }
    }

    def DemonstrateAndCancelReadFile(path: String): IO[Unit] = {
        // will read using resource and cancel
        for {
            fib <- resourceReadFile(path).start // will start fiber that reads file at path
            _ <- IO.sleep(200.milli) >> IO(
              "cancelling fiber"
            ).bebug >> fib.cancel // cancel fiber after 200 milli seconds
        } yield ()
    }

    // nested resources

    def connFromConfResource(path: String) =
        Resource
            .make(IO(s"opening file at $path").bebug >> openFileScanner(path))(scanner =>
                IO(s"closing file at $path").bebug *> IO(scanner.close())
            )
            .flatMap { scanner =>
                Resource.make(IO(Connection(scanner.nextLine())))(conn => conn.close.void)
            }

    def connFromConfResourceClean(path: String) =
        for {
            scanner <- Resource.make(
              IO(s"opening file at $path").bebug >> openFileScanner(path)
            ) { scanner =>
                IO(s"closing file at $path").bebug *> IO(scanner.close())
            }
            conn <- Resource.make(IO(Connection(scanner.nextLine())))(conn => conn.close.void)
        } yield conn

    val openConnection =
        connFromConfResource("~/code/sheeiit.txt").use(conn => conn.open >> IO.never)

    val openConnectionCancel = for {
        fib <- openConnection.start
        _ <- IO.sleep(100.milli) >> fib.cancel
    } yield ()


    //// Finalisers 
    val ioWithFinaliser = IO(s"Getting hold of a resource").bebug.guarantee( IO("Free resource").bebug.void )

    val ioWithFinaliser2 = IO(s"Getting resource 2").bebug.guaranteeCase{
        case Succeeded(fa) => fa.flatMap{v => IO(s"succeeded with value $v").bebug.void}
        case Canceled() => IO("Resource got cancelled , and releasing").bebug.void
        case Errored(e) => IO(s"Nothing to release, an error occured ${e.getMessage()} while acquiring resource").bebug.void
    }
    override def run: IO[Unit] = openConnectionCancel
}

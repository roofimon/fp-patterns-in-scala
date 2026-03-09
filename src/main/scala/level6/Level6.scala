// package level6

// import cats.Monad
// import cats.effect.IO
// import cats.effect.unsafe.implicits.global
// import cats.syntax.all._
// import scala.xml.XML
// import level5.{FileReader, FileWriter, given}

// // Level6: effect-polymorphic RSS pipeline (tagless-final)
// // processFeedTo[F] runs in any F with Monad, FileReader, FileWriter.

// def parseRSS(xmlContent: String): List[(String, String)] =
//   val xml = XML.loadString(xmlContent)
//   (xml \\ "item").toList.map: node =>
//     val title = (node \ "title").text.trim
//     val link = (node \ "link").text.trim
//     (title, link)

// def formatter(items: List[(String, String)]): String =
//   items
//     .map((title, link) => s"Title: $title\nLink: $link\n---")
//     .mkString("\n")

// def processFeedTo[F[_]](inputPath: String, outputPath: String)(using
//     M: Monad[F],
//     R: FileReader[F],
//     W: FileWriter[F]
// ): F[Option[Unit]] =
//   for
//     optRead <- R.read(inputPath)
//     parsed = parseRSS(optRead.getOrElse(""))
//     formatted = formatter(parsed)
//     result <- W.write(outputPath, formatted)
//   yield result

// @main def runLevel6(): Unit =
//   val dataDir = "sample-data/level4"
//   val feeds = List(
//     s"$dataDir/world.xml" -> "world_news.txt",
//     s"$dataDir/tech.xml" -> "tech_news.txt",
//     s"$dataDir/business.xml" -> "business_news.txt"
//   )

//   val program: IO[Unit] =
//     IO.println("Level6: effect-polymorphic RSS pipeline") *>
//       feeds
//         .traverse((inputPath, outputPath) =>
//           processFeedTo[IO](inputPath, outputPath).void
//         )
//         .void

//   program.unsafeRunSync()

import cats.MonadThrow
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.syntax.all._
import scala.xml.XML
import java.nio.file.{Files, Paths}
import scala.io.Source

// Level7: promote errors from Option into the effect's error channel.
// FeedError ADT extends Throwable → works directly with IO / MonadThrow.
// FileReader / FileWriter return F[A] not F[Option[A]].
// processFeedTo requires MonadThrow[F] instead of bare Monad[F].

// ---------------------------------------------------------------------------
// Error ADT
// ---------------------------------------------------------------------------

sealed trait FeedError extends Throwable:
  override def getMessage: String

case class ReadError(path: String) extends FeedError:
  override def getMessage: String = s"Cannot read file: $path"

case class WriteError(path: String) extends FeedError:
  override def getMessage: String = s"Cannot write file: $path"

// ---------------------------------------------------------------------------
// Type classes — Level 7 versions (self-contained, not imported from level5)
// F[A] not F[Option[A]]: failures surface in the error channel
// ---------------------------------------------------------------------------

trait FileReader[F[_]]:
  def read(path: String): F[String]

trait FileWriter[F[_]]:
  def write(path: String, content: String): F[Unit]

// ---------------------------------------------------------------------------
// IO instances — .adaptError converts any Throwable → typed FeedError
// ---------------------------------------------------------------------------

given FileReader[IO] with
  def read(path: String): IO[String] =
    IO.blocking:
      val source = Source.fromFile(path)
      try source.mkString
      finally source.close()
    .adaptError:
        case _ => ReadError(path)

given FileWriter[IO] with
  def write(path: String, content: String): IO[Unit] =
    IO.blocking:
      Files.write(Paths.get(path), content.getBytes)
      ()
    .adaptError:
        case _ => WriteError(path)
    *> IO.println(s"Saved to $path")

// ---------------------------------------------------------------------------
// Pure functions (unchanged from level6)
// ---------------------------------------------------------------------------

def parseRSS(xmlContent: String): List[(String, String)] =
  val xml = XML.loadString(xmlContent)
  (xml \\ "item").toList.map: node =>
    val title = (node \ "title").text.trim
    val link = (node \ "link").text.trim
    (title, link)

def formatter(items: List[(String, String)]): String =
  items
    .map((title, link) => s"Title: $title\nLink: $link\n---")
    .mkString("\n")

// ---------------------------------------------------------------------------
// Effect-polymorphic pipeline — MonadThrow[F] replaces bare Monad[F]
// Returns F[Unit] not F[Option[Unit]]: the type honestly declares failure
// ---------------------------------------------------------------------------

def processFeedTo[F[_]](inputPath: String, outputPath: String)(using
    F: MonadThrow[F],
    R: FileReader[F],
    W: FileWriter[F]
): F[Unit] =
  for
    content <- R.read(inputPath) // raises ReadError — no getOrElse
    parsed = parseRSS(content)
    formatted = formatter(parsed)
    _ <- W.write(outputPath, formatted) // raises WriteError
  yield ()

// ---------------------------------------------------------------------------
// Entry point — errors handled at the edge, not inside the pipeline
// ---------------------------------------------------------------------------

@main def runLevel7(): Unit =
  val dataDir = "sample-data/level4"
  val feeds = List(
    s"$dataDir/world.xml" -> "world_news.txt",
    s"$dataDir/tech.xml" -> "tech_news.txt",
    s"$dataDir/business.xml" -> "business_news.txt"
  )

  val program: IO[Unit] =
    IO.println(
      "Level7: error channel promotion (MonadThrow + FeedError ADT)"
    ) *>
      feeds
        .traverse: (inputPath, outputPath) =>
          processFeedTo[IO](inputPath, outputPath)
            .handleErrorWith:
              case ReadError(path) =>
                IO.println(s"[Error] Could not read: $path")
              case WriteError(path) =>
                IO.println(s"[Error] Could not write: $path")
              case other => IO.raiseError(other)
        .void

  program.unsafeRunSync()

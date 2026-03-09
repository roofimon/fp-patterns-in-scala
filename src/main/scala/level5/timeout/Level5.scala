package level5.timeout

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.syntax.all._
import scala.xml.XML
import java.nio.file.{Files, Paths}
import scala.io.Source
import scala.concurrent.duration.*
import level5.typeclass.given
import level5.typeclass.retry

// Level5: RSS pipeline with FileReader/FileWriter type classes
// Effectful ops abstracted by type class; IO instances + extension methods.

trait FileReader[F[_]]:
  def read(path: String): F[Option[String]]

trait FileWriter[F[_]]:
  def write(path: String, content: String): F[Option[Unit]]

given FileReader[IO] with
  def read(path: String): IO[Option[String]] =
    IO.blocking:
      try
        val source = Source.fromFile(path)
        try Some(source.mkString)
        finally source.close()
      catch case _: Exception => None
    .flatMap {
        case None    => IO.println(s"[Error] Failed to read $path").as(None)
        case Some(s) => IO.some(s)
      }

given FileWriter[IO] with
  def write(path: String, content: String): IO[Option[Unit]] =
    IO.blocking:
      try
        Files.write(Paths.get(path), content.getBytes)
        Some(())
      catch case _: Exception => None
    .flatMap: opt =>
        opt.fold(IO.println(s"[Error] Failed to save to $path").as(None))(_ =>
          IO.println(s"✅ Saved to $path").as(Some(()))
        )

extension (path: String)(using R: FileReader[IO])
  def readContent: IO[Option[String]] = R.read(path)

extension (content: String)(using W: FileWriter[IO])
  def writeTo(path: String): IO[Option[Unit]] = W.write(path, content)

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

val readTimeout = 10.seconds

def processFeedTo(inputPath: String, outputPath: String)(using
    FileReader[IO],
    FileWriter[IO]
): IO[Option[Unit]] =
  for
    optRead <- inputPath.readContent
      .timeout(readTimeout)
      .retry(3)
      .handleErrorWith(e =>
        IO.println(s"[Timeout/Error after retries] $inputPath: ${e.getMessage}")
          .as(None)
      )
    parsed = parseRSS(optRead.getOrElse(""))
    formatted = formatter(parsed)
    result <- formatted.writeTo(outputPath)
  yield result

@main def runLevel5RSS(): Unit =
  val dataDir = "sample-data/level4"
  val feeds = List(
    s"$dataDir/world.xml" -> "world_news.txt",
    s"$dataDir/tech.xml" -> "tech_news.txt",
    s"$dataDir/business.xml" -> "business_news.txt"
  )

  val program: IO[Unit] =
    IO.println("Level5: RSS from local files (IO Monad + type classes)") *>
      feeds
        .traverse((inputPath, outputPath) =>
          processFeedTo(inputPath, outputPath).void
        )
        .void

  program.unsafeRunSync()

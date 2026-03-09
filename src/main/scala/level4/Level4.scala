package level4

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.syntax.all._
import scala.xml.XML
import java.nio.file.{Files, Paths}
import scala.io.Source
import scala.concurrent.duration.*
//============================================================================
// IO Monad: effectful operations return IO[A]; composition is pure until run.
//============================================================================
def readFromFile(filePath: String): IO[Option[String]] =
  IO.blocking:
    try
      val source = Source.fromFile(filePath)
      try Some(source.mkString)
      finally source.close()
    catch
      case e: Exception =>
        None
  .flatMap {
      case None    => IO.println(s"[Error] Failed to read $filePath").as(None)
      case Some(s) => IO.some(s)
    }

def saveToFile(path: String, content: String): IO[Option[Unit]] =
  IO.blocking:
    try
      Files.write(Paths.get(path), content.getBytes)
      Some(())
    catch case e: Exception => None
  .flatMap: opt =>
      opt.fold(IO.println(s"[Error] Failed to save to $path").as(None))(_ =>
        IO.println(s"✅ Saved to $path").as(Some(()))
      )
//============================================================================
// Pure: parse XML (empty list on parse failure)
//============================================================================
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

//============================================================================
// Orchestrate one feed: read file (IO, with timeout) -> parse (pure) -> format (pure) -> save (IO)
//============================================================================
val readTimeout = 10.seconds

def processFeedTo(inputPath: String, outputPath: String): IO[Option[Unit]] =
  for
    optRead <- readFromFile(inputPath)
      .timeoutTo(readTimeout, IO.println(s"[Timeout] Read $inputPath exceeded $readTimeout").as(None))
    parsed: List[(String, String)] = parseRSS(optRead.getOrElse(""))
    formatted: String = formatter(parsed)
    result <- saveToFile(outputPath, formatted)
  yield result

@main def runLevel4(): Unit =
  val dataDir = "sample-data/level4"
  val feeds = List(
    s"$dataDir/world.xml" -> "world_news.txt",
    s"$dataDir/tech.xml" -> "tech_news.txt",
    s"$dataDir/business.xml" -> "business_news.txt"
  )

  val program: IO[Unit] =
    IO.println("Level4: RSS from local files (IO Monad)") *>
      feeds
        .traverse((inputPath, outputPath) =>
          processFeedTo(inputPath, outputPath).void
        )
        .void

  program.unsafeRunSync()

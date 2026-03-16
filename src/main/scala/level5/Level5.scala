package level5

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.syntax.all._
import scala.xml.XML
import java.nio.file.{Files, Paths}
import scala.io.Source

// Level5: RSS pipeline with FileReader/FileWriter type classes
// Effectful ops abstracted by type class; IO instances + extension methods.

trait FeedReader[F[_], In]:
  def read(input: In): F[Option[String]]

final case class LocalPath(value: String)
final case class RemoteUrl(value: String)

trait ContentWriter[F[_]]:
  def write(path: String, content: String): F[Option[Unit]]

given FeedReader[IO, LocalPath] with
  def read(input: LocalPath): IO[Option[String]] =
    val path = input.value
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

given FeedReader[IO, RemoteUrl] with
  def read(input: RemoteUrl): IO[Option[String]] =
    val url = input.value
    IO.blocking:
      try
        val source = Source.fromURL(url)
        try Some(source.mkString)
        finally source.close()
      catch case _: Exception => None
    .flatMap {
        case None    => IO.println(s"[Error] Failed to read $url").as(None)
        case Some(s) => IO.some(s)
      }

given ContentWriter[IO] with
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

extension [In](input: In)(using R: FeedReader[IO, In])
  def readContent: IO[Option[String]] = R.read(input)

extension (content: String)(using W: ContentWriter[IO])
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

def processFeedTo[In](input: In, outputPath: String)(using
    FeedReader[IO, In],
    ContentWriter[IO]
): IO[Option[Unit]] =
  for
    optRead <- input.readContent
    result <- optRead match
      case Some(content) =>
        val parsed = parseRSS(content)
        val formatted = formatter(parsed)
        formatted.writeTo(outputPath)
      case None => IO.pure(None)
  yield result

@main def runLevel5RSS(): Unit =
  val dataDir = "sample-data/level4"
  val feeds = List(
    LocalPath(s"$dataDir/world.xml") -> "world_news.txt",
    LocalPath(s"$dataDir/tech.xml") -> "tech_news.txt",
    LocalPath(s"$dataDir/business.xml") -> "business_news.txt"
  )

  val program: IO[Unit] =
    IO.println("Level5: RSS from local files (IO Monad + type classes)") *>
      feeds
        .traverse((input, outputPath) => processFeedTo(input, outputPath).void)
        .void

  program.unsafeRunSync()

@main def runLevel5RSSFromRemote(): Unit =
  val remoteFeeds = List(
    RemoteUrl(
      "https://feeds.bbci.co.uk/news/technology/rss.xml"
    ) -> "remote_tech_news.txt",
    RemoteUrl(
      "https://feeds.bbci.co.uk/news/business/rss.xml"
    ) -> "remote_business_news.txt"
  )

  val program: IO[Unit] =
    IO.println("Level5: RSS from remote URLs (IO Monad + type classes)") *>
      remoteFeeds
        .traverse((input, outputPath) => processFeedTo(input, outputPath).void)
        .void

  program.unsafeRunSync()

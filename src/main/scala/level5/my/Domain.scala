package level5.my

import cats.data.EitherT
import cats.effect.IO
import cats.syntax.all._

// ---------------------------------------------------------------------------
// Domain (Core Business Logic)
// ---------------------------------------------------------------------------
// Pure business logic and data structures.
// Completely independent of IO, databases, or frameworks.

case class RemoteRss(url: String)
case class LocalRss(path: String)
case class LocalFileLocation(path: String)

case class RSSItem(title: String, link: String)

sealed trait FeedError
object FeedError:
  case class ReadError(source: String, message: String) extends FeedError
  case class ParseError(raw: String, message: String) extends FeedError
  case class WriteError(target: String, message: String) extends FeedError

trait FeedReader[F[_], A]:
  def readFeed(input: A): F[Either[FeedError, String]]

trait ContentWriter[F[_], B]:
  def write(path: B, content: String): F[Either[FeedError, Unit]]

given consoleWriter: ContentWriter[IO, LocalFileLocation] with
  def write(
      local: LocalFileLocation,
      content: String
  ): IO[Either[FeedError, Unit]] =
    if local.path.contains("forbidden") then
      IO.pure(Left(FeedError.WriteError(local.path, "Permission denied")))
    else IO.println(s"💾 Saving output to ${local.path}") *> IO.pure(Right(()))

// An instance for reading from a remote URL
given remoteFeedReader: FeedReader[IO, RemoteRss] with
  def readFeed(remote: RemoteRss): IO[Either[FeedError, String]] =
    if remote.url.contains("invalid") then
      IO.pure(
        Left(FeedError.ReadError(remote.url, "Remote endpoint unavailable"))
      )
    else
      IO.println(s"🌐 Fetching from URL: ${remote.url}") *> IO.pure(
        Right("Remote Headline, https://example.com/remote")
      )

// An instance for reading from a local File
given fileFeedReader: FeedReader[IO, LocalRss] with
  def readFeed(file: LocalRss): IO[Either[FeedError, String]] =
    if file.path.contains("missing") then
      IO.pure(Left(FeedError.ReadError(file.path, "Local file missing")))
    else
      IO.println(s"📂 Reading file: ${file.path}") *> IO.pure(
        Right("Local Headline, https://example.com/local")
      )

extension [A](input: A)
  def readFeed[F[_]](using
      reader: FeedReader[F, A]
  ): F[Either[FeedError, String]] =
    reader.readFeed(input)

//Pure domain logic for parsing and formatting RSS feeds
def parseRSS(content: String): Either[FeedError, List[RSSItem]] =
  content.split(",").map(_.trim).toList match
    case title :: link :: Nil if title.nonEmpty && link.nonEmpty =>
      Right(List(RSSItem(title, link)))
    case _ =>
      Left(FeedError.ParseError(content, "Expected format: title, link"))

def format(items: List[RSSItem]): String =
  items
    .map(item => s"Title: ${item.title} Link: ${item.link}\n---")
    .mkString("\n")

def processFeed[A, B](input: A, outputPath: B)(using
    reader: FeedReader[IO, A],
    writer: ContentWriter[IO, B]
): IO[Either[FeedError, Unit]] =
  (for
    content <- EitherT(input.readFeed)
    parsedItems <- EitherT.fromEither[IO](parseRSS(content))
    _ <- EitherT(writer.write(outputPath, format(parsedItems)))
  yield ()).value

def processFeeds[A, B](feeds: List[(A, B)])(using
    reader: FeedReader[IO, A],
    writer: ContentWriter[IO, B]
): IO[List[Either[FeedError, Unit]]] =
  feeds.traverse((input, outputPath) => processFeed(input, outputPath))

@main def run(): Unit =
  import cats.effect.unsafe.implicits.global

  val localFeeds = List(
    LocalRss("sample-data/level4/tech.xml") -> LocalFileLocation(
      "output/local_feed.txt"
    ),
    LocalRss("missing/file.xml") -> LocalFileLocation(
      "output/local_missing.txt"
    )
  )

  val remoteFeeds = List(
    RemoteRss("https://feeds.bbci.co.uk/news") -> LocalFileLocation(
      "output/news.txt"
    ),
    RemoteRss("https://invalid.example.com/rss") -> LocalFileLocation(
      "output/news_fail.txt"
    )
  )

  val program = for
    remoteResults <- processFeeds(remoteFeeds)
    _ <- IO.println(s"Remote results: $remoteResults")
    localResults <- processFeeds(localFeeds)
    _ <- IO.println(s"Local results: $localResults")
  yield ()

  program.unsafeRunSync()

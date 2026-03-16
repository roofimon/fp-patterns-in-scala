package level6

import cats.Monoid
import cats.effect.IO
import cats.syntax.all._

// ---------------------------------------------------------------------------
// Domain + Errors
// ---------------------------------------------------------------------------

case class RemoteRss(url: String)
case class LocalRss(path: String)
case class LocalFileLocation(path: String)

case class RSSItem(title: String, link: String)

sealed trait FeedError
object FeedError:
  case class ReadError(source: String, message: String) extends FeedError
  case class ParseError(raw: String, message: String) extends FeedError
  case class WriteError(target: String, message: String) extends FeedError

case class ProcessSummary(
    total: Int,
    readFailures: Int,
    parseFailures: Int,
    writeFailures: Int,
    successes: Int
)

object ProcessSummary:
  val success: ProcessSummary = ProcessSummary(1, 0, 0, 0, 1)

  def readFailure: ProcessSummary = ProcessSummary(1, 1, 0, 0, 0)
  def parseFailure: ProcessSummary = ProcessSummary(1, 0, 1, 0, 0)
  def writeFailure: ProcessSummary = ProcessSummary(1, 0, 0, 1, 0)

  given Monoid[ProcessSummary] with
    def empty: ProcessSummary = ProcessSummary(0, 0, 0, 0, 0)
    def combine(a: ProcessSummary, b: ProcessSummary): ProcessSummary =
      ProcessSummary(
        total = a.total + b.total,
        readFailures = a.readFailures + b.readFailures,
        parseFailures = a.parseFailures + b.parseFailures,
        writeFailures = a.writeFailures + b.writeFailures,
        successes = a.successes + b.successes
      )

case class ProcessResult(
    source: String,
    target: String,
    result: Either[FeedError, Unit]
):
  val summary: ProcessSummary = result match
    case Right(_)                      => ProcessSummary.success
    case Left(_: FeedError.ReadError)  => ProcessSummary.readFailure
    case Left(_: FeedError.ParseError) => ProcessSummary.parseFailure
    case Left(_: FeedError.WriteError) => ProcessSummary.writeFailure

// ---------------------------------------------------------------------------
// Ports
// ---------------------------------------------------------------------------

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

// ---------------------------------------------------------------------------
// Pure domain functions
// ---------------------------------------------------------------------------

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

// ---------------------------------------------------------------------------
// Monad: dependent composition (read -> parse -> format -> write)
// ---------------------------------------------------------------------------

def processFeed[A, B](input: A, outputPath: B)(using
    reader: FeedReader[IO, A],
    writer: ContentWriter[IO, B]
): IO[ProcessResult] =
  for
    contentOrError <- input.readFeed
    result <- contentOrError match
      case Left(readError) => IO.pure(Left(readError))
      case Right(content)  =>
        parseRSS(content) match
          case Left(parseError) => IO.pure(Left(parseError))
          case Right(parsed)    =>
            val formatted = format(parsed)
            writer.write(outputPath, formatted)
  yield ProcessResult(input.toString, outputPath.toString, result)

// ---------------------------------------------------------------------------
// Applicative: independent composition
// ---------------------------------------------------------------------------

def readTwoIndependent(
    remote: RemoteRss,
    local: LocalRss
): IO[(Either[FeedError, String], Either[FeedError, String])] =
  (remote.readFeed[IO], local.readFeed[IO]).mapN((remoteResult, localResult) =>
    (remoteResult, localResult)
  )

// ---------------------------------------------------------------------------
// Monoid: aggregate many process results into one summary
// ---------------------------------------------------------------------------

def processBatch[A, B](feeds: List[(A, B)])(using
    reader: FeedReader[IO, A],
    writer: ContentWriter[IO, B]
): IO[(List[ProcessResult], ProcessSummary)] =
  feeds
    .traverse((input, outputPath) => processFeed(input, outputPath))
    .map(results => (results, results.map(_.summary).combineAll))

val processLocalFile =
  processFeed(
    LocalRss("sample-data/level4/tech.xml"),
    LocalFileLocation("output/local_feed.txt")
  )

val processRemoteUrl =
  processFeed(
    RemoteRss("https://feeds.bbci.co.uk/news"),
    LocalFileLocation("output/news.txt")
  )

@main def run(): Unit =
  import cats.effect.unsafe.implicits.global

  val localFeeds = List(
    LocalRss("sample-data/level4/tech.xml") -> LocalFileLocation(
      "output/level6_local_tech.txt"
    ),
    LocalRss("missing/file.xml") -> LocalFileLocation(
      "output/level6_local_missing.txt"
    )
  )

  val remoteFeeds = List(
    RemoteRss("https://feeds.bbci.co.uk/news") -> LocalFileLocation(
      "output/level6_remote_news.txt"
    ),
    RemoteRss("https://invalid.example.com/rss") -> LocalFileLocation(
      "output/level6_remote_fail.txt"
    )
  )

  val program = for
    independentReads <- readTwoIndependent(
      RemoteRss("https://feeds.bbci.co.uk/news"),
      LocalRss("sample-data/level4/tech.xml")
    )
    _ <- IO.println(s"Applicative preview (remote/local): $independentReads")

    localRun <- processBatch(localFeeds)
    (localResults, localSummary) = localRun
    _ <- IO.println(s"Local results: ${localResults.map(_.result)}")
    _ <- IO.println(s"Local summary: $localSummary")

    remoteRun <- processBatch(remoteFeeds)
    (remoteResults, remoteSummary) = remoteRun
    _ <- IO.println(s"Remote results: ${remoteResults.map(_.result)}")
    _ <- IO.println(s"Remote summary: $remoteSummary")

    totalSummary = localSummary |+| remoteSummary
    _ <- IO.println(s"Combined summary (Monoid): $totalSummary")

    _ <- processRemoteUrl
    _ <- processLocalFile
  yield ()

  program.unsafeRunSync()

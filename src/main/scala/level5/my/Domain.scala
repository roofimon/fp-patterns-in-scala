package level5.my
import cats.effect.IO

// ---------------------------------------------------------------------------
// Domain (Core Business Logic)
// ---------------------------------------------------------------------------
// Pure business logic and data structures.
// Completely independent of IO, databases, or frameworks.
case class RemoteUrl(url: String)
case class LocalFile(path: String)

case class RSSItem(title: String, link: String)

trait FeedReader[F[_], A]:
  def readFeed(input: A): F[Either[Error, String]]

trait ContentWriter[F[_]]:
  def write(path: String, content: String): F[Unit]

given consoleWriter: ContentWriter[IO] with
  def write(path: String, content: String): IO[Unit] =
    IO.println(s"💾 Saving $content JSON to $path") *> IO.unit

// An instance for reading from a remote URL
given remoteFeedReader: FeedReader[IO, RemoteUrl] with
  def readFeed(remote: RemoteUrl): IO[Either[Error, String]] =
    IO.println(s"🌐 Fetching from URL: ${remote.url}") *> IO.pure(
      Right(
        s"Some Title, http://example.com"
      ) // Dummy content for demonstration
      // Left(Error(s"Failed to fetch from ${remote.url}"))
    )

// An instance for reading from a local File
given fileFeedReader: FeedReader[IO, LocalFile] with
  def readFeed(file: LocalFile): IO[Either[Error, String]] =
    IO.println(s"📂 Reading file: ${file.path}") *> IO.pure(
      // Right(s"Content of ${file.path}")
      Left(Error(s"Failed to read file ${file.path}"))
    )

extension [A](input: A)
  def readFeed[F[_]](using reader: FeedReader[F, A]): F[Either[Error, String]] =
    reader.readFeed(input)

//Pure domain logic for parsing and formatting RSS feeds
def parseRSS(content: String): List[RSSItem] =
  // Dummy parser for demonstration
  val token = content.split(",")
  List(RSSItem(token.head, token.last))

def format(items: List[RSSItem]): String =
  items
    .map(item => s"Title: ${item.title} Link: ${item.link}\n---")
    .mkString("\n")

def processFeed[A](input: A, outputPath: String)(using
    reader: FeedReader[IO, A],
    writer: ContentWriter[IO]
): IO[Unit] =
  for
    maybeContent <- input.readFeed
    content = maybeContent.getOrElse("Error,No content")
    parsed = parseRSS(content)
    formatted = format(parsed)
    _ <- writer.write(outputPath, formatted)
  yield ()

val processLocalFile =
  processFeed(LocalFile("path/to/local/file.txt"), "output/local_feed.txt")
val processRemoteUrl =
  processFeed(RemoteUrl("https://feeds.bbci.co.uk/news"), "output/news.txt")

@main def run(): Unit =
  import cats.effect.unsafe.implicits.global
  // The compiler finds 'stringFeedReader' automatically
  processRemoteUrl.unsafeRunSync()
  // The compiler finds 'fileFeedReader' automatically
  processLocalFile.unsafeRunSync()

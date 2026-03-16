package level5.paa
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.syntax.all._

// ---------------------------------------------------------------------------
// Main Entry Point
// ---------------------------------------------------------------------------
// Manual Dependency Injection: wires adapters into the application layer.

val processLocalFile =
  processFeed(
    LocalRss("path/to/local/file.txt"),
    LocalFileLocation("output/local_feed.txt")
  )
val processRemoteUrl =
  processFeed(
    RemoteRss("https://feeds.bbci.co.uk/news"),
    LocalFileLocation("output/news.txt")
  )

@main def run(): Unit =
  import cats.effect.unsafe.implicits.global
  // The compiler finds 'stringFeedReader' automatically
  processRemoteUrl.unsafeRunSync()
  // The compiler finds 'fileFeedReader' automatically
  processLocalFile.unsafeRunSync()

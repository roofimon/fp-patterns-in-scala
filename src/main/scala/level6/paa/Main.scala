package level5.paa
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.syntax.all._

// ---------------------------------------------------------------------------
// Main Entry Point
// ---------------------------------------------------------------------------
// Manual Dependency Injection: wires adapters into the application layer.

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

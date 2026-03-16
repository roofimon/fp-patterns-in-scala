package level5.pad

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.syntax.all._

// Wiring: compose adapters and run. Reader is passed explicitly; writer is wired via given.

@main def runLevel5FromFile(): Unit =

  println("Level5.pad: RSS from file (ContentReader + explicit adapter)")
  val dataDir = "sample-data/level4"
  val feeds = List(
    s"$dataDir/world.xml" -> "output/world_news.txt",
    s"$dataDir/tech.xml" -> "output/tech_news.txt",
    s"$dataDir/business.xml" -> "output/business_news.txt"
  )

  val program: IO[Unit] =
    IO.println(
      "Level5.pad: RSS from file (ContentReader + explicit adapter)"
    ) *>
      feeds
        .traverse((inputPath, outputPath) =>
          processFeedTo(inputPath, outputPath)(FileSystemReaderAdapter).void
        )
        .void

  program.unsafeRunSync()

@main def runLevel5FromRss(): Unit =
  val feeds = List(
    "https://feeds.bbci.co.uk/news/world/rss.xml" -> "output/world_rss.txt",
    "https://feeds.bbci.co.uk/news/technology/rss.xml" -> "output/tech_rss.txt",
    "https://feeds.bbci.co.uk/news/business/rss.xml" -> "output/business_rss.txt"
  )

  val program: IO[Unit] =
    IO.println(
      "Level5.pad: RSS from URL (ContentReader + explicit adapter)"
    ) *>
      feeds
        .traverse((rssUrl, outputPath) =>
          processFeedTo(rssUrl, outputPath)(RssReadAdapter).void
        )
        .void

  program.unsafeRunSync()

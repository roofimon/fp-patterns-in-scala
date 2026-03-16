package level5.paa
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.syntax.all._

// ---------------------------------------------------------------------------
// Main Entry Point
// ---------------------------------------------------------------------------
// Manual Dependency Injection: wires adapters into the application layer.

@main def runHexagonal(): Unit =
  // 1. Instantiate Adapters (Infrastructure)
  val reader = new FileSystemFeedReader()
  val writer = new FileSystemFeedWriter()

  // 2. Inject Adapters into Application
  val pipeline = new RSSPipeline[IO, LocalPath](reader, writer)

  // 3. Configure and Run
  val dataDir = "sample-data/level4"
  val feeds = List(
    LocalPath(s"$dataDir/world.xml") -> "output/world_news.txt",
    LocalPath(s"$dataDir/tech.xml") -> "output/tech_news.txt",
    LocalPath(s"$dataDir/business.xml") -> "output/business_news.txt"
  )

  val program: IO[Unit] =
    IO.println("--- Starting Hexagonal RSS Pipeline ---") *>
      feeds
        .traverse((input, outputPath) => pipeline.process(input, outputPath))
        .void *>
      IO.println("--- Hexagonal RSS Pipeline Finished ---")

  program.unsafeRunSync()

@main def runHexagonalRemote(): Unit =
  val reader = new RemoteFeedReader()
  val writer = new FileSystemFeedWriter()

  val pipeline = new RSSPipeline[IO, RemoteUrl](reader, writer)

  val feeds = List(
    RemoteUrl(
      "https://feeds.bbci.co.uk/news/technology/rss.xml"
    ) -> "output/remote_tech_news.txt",
    RemoteUrl(
      "https://feeds.bbci.co.uk/news/business/rss.xml"
    ) -> "output/remote_business_news.txt"
  )

  val program: IO[Unit] =
    IO.println("--- Starting Remote Hexagonal RSS Pipeline ---") *>
      feeds
        .traverse((input, outputPath) => pipeline.process(input, outputPath))
        .void *>
      IO.println("--- Remote Hexagonal RSS Pipeline Finished ---")

  program.unsafeRunSync()

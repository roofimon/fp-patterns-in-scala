package level6.paa

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
  val pipeline = new RSSPipeline[IO](reader, writer)

  // 3. Configure and Run
  val dataDir = "sample-data/level4"
  val feeds = List(
    s"$dataDir/world.xml" -> "output/world_news.txt",
    s"$dataDir/tech.xml" -> "output/tech_news.txt",
    s"$dataDir/business.xml" -> "output/business_news.txt"
  )

  val program: IO[Unit] =
    IO.println("--- Starting Hexagonal RSS Pipeline ---") *>
      feeds
        .traverse((inputPath, outputPath) => pipeline.process(inputPath, outputPath))
        .void *>
      IO.println("--- Hexagonal RSS Pipeline Finished ---")

  program.unsafeRunSync()

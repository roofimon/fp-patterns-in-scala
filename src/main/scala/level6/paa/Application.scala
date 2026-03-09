package level6.paa

import cats.Monad
import cats.syntax.all._

// ---------------------------------------------------------------------------
// Application (Use Case / Service Layer)
// ---------------------------------------------------------------------------
// Orchestrates the flow by coordinating between ports and domain logic.
// Accepts dependencies through constructor injection (Dependency Inversion).

class RSSPipeline[F[_]: Monad](reader: FeedReader[F], writer: FeedWriter[F]):
  def process(inputPath: String, outputPath: String): F[Option[Unit]] =
    for
      optContent <- reader.read(inputPath)
      result <- optContent match
        case Some(content) =>
          val items = RSSLogic.parse(content)
          val formatted = RSSLogic.format(items)
          writer.write(outputPath, formatted)
        case None =>
          Monad[F].pure(None)
    yield result

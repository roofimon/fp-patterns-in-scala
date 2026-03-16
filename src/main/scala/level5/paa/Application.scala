package level5.paa

import cats.Monad
import cats.syntax.all._

// ---------------------------------------------------------------------------
// Application (Use Case / Service Layer)
// ---------------------------------------------------------------------------
// Orchestrates the flow by coordinating between ports and domain logic.
// Accepts dependencies through constructor injection (Dependency Inversion).

class RSSPipeline[F[_]: Monad, In](
    reader: FeedReader[F, In],
    writer: FeedWriter[F]
):
  def process(input: In, outputPath: String): F[Option[Unit]] =
    for
      optContent <- reader.read(input)
      result <- optContent match
        case Some(content) =>
          val items = RSSLogic.parse(content)
          val formatted = RSSLogic.format(items)
          writer.write(outputPath, formatted)
        case None =>
          Monad[F].pure(None)
    yield result

package level5.paa

import cats.Monad
import cats.syntax.all._
import cats.effect.IO
// ---------------------------------------------------------------------------
// Application (Use Case / Service Layer)
// ---------------------------------------------------------------------------
// Orchestrates the flow by coordinating between ports and domain logic.
// Accepts dependencies through constructor injection (Dependency Inversion).

def processFeed[A, B](input: A, outputPath: B)(using
    reader: FeedReader[IO, A],
    writer: ContentWriter[IO, B]
): IO[Unit] =
  for
    maybeContent <- input.readFeed
    content = maybeContent.getOrElse("Error,No content")
    parsed = parseRSS(content)
    formatted = format(parsed)
    _ <- writer.write(outputPath, formatted)
  yield ()

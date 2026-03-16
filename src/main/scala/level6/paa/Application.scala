package level5.paa

import cats.effect.IO
import cats.data.EitherT
import cats.Monad
import cats.syntax.all._

// ---------------------------------------------------------------------------
// Application (Use Case / Service Layer)
// ---------------------------------------------------------------------------
// Orchestrates the flow by coordinating between ports and domain logic.
// Accepts dependencies through constructor injection (Dependency Inversion).
def processFeed[A, B](input: A, outputPath: B)(using
    reader: FeedReader[IO, A],
    writer: ContentWriter[IO, B]
): IO[Either[FeedError, Unit]] =
  (for
    content <- EitherT(input.readFeed)
    parsedItems <- EitherT.fromEither[IO](parseRSS(content))
    _ <- EitherT(writer.write(outputPath, format(parsedItems)))
  yield ()).value

def processFeeds[A, B](feeds: List[(A, B)])(using
    reader: FeedReader[IO, A],
    writer: ContentWriter[IO, B]
): IO[List[Either[FeedError, Unit]]] =
  feeds.traverse((input, outputPath) => processFeed(input, outputPath))

package level5.paa

// ---------------------------------------------------------------------------
// Ports (Interfaces)
// ---------------------------------------------------------------------------
// Define the contracts that the application requires.
// These are the boundaries between the core and the outside world.

trait FeedReader[F[_], A]:
  def readFeed(input: A): F[Either[Error, String]]

trait ContentWriter[F[_], B]:
  def write(path: B, content: String): F[Unit]

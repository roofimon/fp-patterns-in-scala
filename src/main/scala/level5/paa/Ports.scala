package level5.paa

// ---------------------------------------------------------------------------
// Ports (Interfaces)
// ---------------------------------------------------------------------------
// Define the contracts that the application requires.
// These are the boundaries between the core and the outside world.

trait FeedReader[F[_], In]:
  def read(input: In): F[Option[String]]

trait FeedWriter[F[_]]:
  def write(path: String, content: String): F[Option[Unit]]

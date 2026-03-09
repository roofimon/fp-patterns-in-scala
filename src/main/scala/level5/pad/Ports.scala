package level5.pad

// Ports (driven: read/write content).
// Interfaces the core depends on; adapters provide implementations.

trait ContentReader[F[_]]:
  def read(path: String): F[Option[String]]

trait FileWriter[F[_]]:
  def write(path: String, content: String): F[Option[Unit]]

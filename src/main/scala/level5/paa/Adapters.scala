package level5.paa

import cats.effect.IO
import java.nio.file.{Files, Paths}
import scala.io.Source

// ---------------------------------------------------------------------------
// Adapters (Infrastructure)
// ---------------------------------------------------------------------------
// Concrete implementations of the ports for specific technologies.
// These contain all the technical details (File System, Database, HTTP, etc.)

given consoleWriter: ContentWriter[IO, LocalFileLocation] with
  def write(local: LocalFileLocation, content: String): IO[Unit] =
    IO.println(s"💾 Saving $content JSON to ${local.path}") *> IO.unit

// An instance for reading from a remote URL
given remoteFeedReader: FeedReader[IO, RemoteRss] with
  def readFeed(remote: RemoteRss): IO[Either[Error, String]] =
    IO.println(s"🌐 Fetching from URL: ${remote.url}") *> IO.pure(
      Right(
        s"Some Title, http://example.com"
      )
      // Left(Error(s"Failed to fetch from ${remote.url}"))
    )

// An instance for reading from a local File
given fileFeedReader: FeedReader[IO, LocalRss] with
  def readFeed(file: LocalRss): IO[Either[Error, String]] =
    IO.println(s"📂 Reading file: ${file.path}") *> IO.pure(
      Right(s"Content of ${file.path}")
      // Left(Error(s"Failed to read file ${file.path}"))
    )

extension [A](input: A)
  def readFeed[F[_]](using reader: FeedReader[F, A]): F[Either[Error, String]] =
    reader.readFeed(input)

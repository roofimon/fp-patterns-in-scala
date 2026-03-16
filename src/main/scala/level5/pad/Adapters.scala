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
  def write(
      local: LocalFileLocation,
      content: String
  ): IO[Either[FeedError, Unit]] =
    if local.path.contains("forbidden") then
      IO.pure(Left(FeedError.WriteError(local.path, "Permission denied")))
    else IO.println(s"💾 Saving output to ${local.path}") *> IO.pure(Right(()))

// An instance for reading from a remote URL
given remoteFeedReader: FeedReader[IO, RemoteRss] with
  def readFeed(remote: RemoteRss): IO[Either[FeedError, String]] =
    if remote.url.contains("invalid") then
      IO.pure(
        Left(FeedError.ReadError(remote.url, "Remote endpoint unavailable"))
      )
    else
      IO.println(s"🌐 Fetching from URL: ${remote.url}") *> IO.pure(
        Right("Remote Headline, https://example.com/remote")
      )

// An instance for reading from a local File
given fileFeedReader: FeedReader[IO, LocalRss] with
  def readFeed(file: LocalRss): IO[Either[FeedError, String]] =
    if file.path.contains("missing") then
      IO.pure(Left(FeedError.ReadError(file.path, "Local file missing")))
    else
      IO.println(s"📂 Reading file: ${file.path}") *> IO.pure(
        Right("Local Headline, https://example.com/local")
      )

extension [A](input: A)
  def readFeed[F[_]](using
      reader: FeedReader[F, A]
  ): F[Either[FeedError, String]] =
    reader.readFeed(input)

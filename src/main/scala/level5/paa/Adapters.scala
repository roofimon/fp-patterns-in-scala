package level5.paa

import cats.effect.IO
import java.nio.file.{Files, Paths}
import scala.io.Source

// ---------------------------------------------------------------------------
// Adapters (Infrastructure)
// ---------------------------------------------------------------------------
// Concrete implementations of the ports for specific technologies.
// These contain all the technical details (File System, Database, HTTP, etc.)

class FileSystemFeedReader extends FeedReader[IO, LocalPath]:
  def read(input: LocalPath): IO[Option[String]] =
    val path = input.value
    IO.blocking:
      try
        val source = Source.fromFile(path)
        try Some(source.mkString)
        finally source.close()
      catch case _: Exception => None
    .flatMap:
        case None => IO.println(s"[Error] Failed to read $path").as(None)
        case some => IO.pure(some)

class RemoteFeedReader extends FeedReader[IO, RemoteUrl]:
  def read(input: RemoteUrl): IO[Option[String]] =
    val url = input.value
    IO.blocking:
      try
        val source = Source.fromURL(url)
        try Some(source.mkString)
        finally source.close()
      catch case _: Exception => None
    .flatMap:
        case None => IO.println(s"[Error] Failed to read $url").as(None)
        case some => IO.pure(some)

class FileSystemFeedWriter extends FeedWriter[IO]:
  def write(path: String, content: String): IO[Option[Unit]] =
    IO.blocking:
      try
        Files.write(Paths.get(path), content.getBytes)
        Some(())
      catch case _: Exception => None
    .flatMap:
        case None => IO.println(s"[Error] Failed to save to $path").as(None)
        case some => IO.println(s"✅ Saved to $path").as(some)

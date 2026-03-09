package level5.pad

import cats.effect.IO
import java.nio.file.{Files, Paths}
import scala.io.Source

// Adapters: file system implementation of ports.

object FileSystemReaderAdapter extends ContentReader[IO]:
  def read(path: String): IO[Option[String]] =
    IO.blocking:
      try
        val source = Source.fromFile(path)
        try Some(source.mkString)
        finally source.close()
      catch case _: Exception => None
    .flatMap {
      case None    => IO.println(s"[Error] Failed to read $path").as(None)
      case Some(s) => IO.some(s)
    }

object FileSystemWriterAdapter extends FileWriter[IO]:
  def write(path: String, content: String): IO[Option[Unit]] =
    IO.blocking:
      try
        Files.write(Paths.get(path), content.getBytes)
        Some(())
      catch case _: Exception => None
    .flatMap: opt =>
      opt.fold(IO.println(s"[Error] Failed to save to $path").as(None))(_ =>
        IO.println(s"✅ Saved to $path").as(Some(()))
      )

given FileWriter[IO] = FileSystemWriterAdapter

extension (content: String)(using W: FileWriter[IO])
  def writeTo(path: String): IO[Option[Unit]] = W.write(path, content)

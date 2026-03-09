package level5.pad

import cats.effect.IO
import scala.io.Source

object RssReadAdapter extends ContentReader[IO]:
  def read(path: String): IO[Option[String]] =
    IO.blocking:
      try
        val source = Source.fromURL(path)
        try Some(source.mkString)
        finally source.close()
      catch case _: Exception => None
    .flatMap {
      case None    => IO.println(s"[Error] Failed to read $path").as(None)
      case Some(s) => IO.some(s)
    }

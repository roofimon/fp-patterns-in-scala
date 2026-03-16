package level5.pad

import cats.effect.IO
import cats.syntax.all._
import scala.xml.XML

// Core: use case + pure domain. Depends only on port interfaces.

def parseRSS(xmlContent: String): List[(String, String)] =
  val xml = XML.loadString(xmlContent)
  (xml \\ "item").toList.map: node =>
    val title = (node \ "title").text.trim
    val link = (node \ "link").text.trim
    (title, link)

def formatter(items: List[(String, String)]): String =
  items
    .map((title, link) => s"Title: $title\nLink: $link\n---")
    .mkString("\n")

def processFeedTo(inputPath: String, outputPath: String)(reader: ContentReader[IO])(using
    FileWriter[IO]
): IO[Option[Unit]] =
  for
    optRead <- reader.read(inputPath)
    parsed  = parseRSS(optRead.getOrElse(""))
    formatted = formatter(parsed)
    result <- formatted.writeTo(outputPath)
  yield result

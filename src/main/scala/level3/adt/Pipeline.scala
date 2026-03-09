package level3.adt

import scala.xml.XML
import java.nio.file.{Files, Paths}
import scala.io.Source
import level3.adt.Level3ADT.Job

case class FeedItem(title: String, link: String)

def fetchData(url: String): Option[String] =
  try
    val source = Source.fromURL(url)
    try
      Some(source.mkString)
    finally
      source.close()
  catch
    case e: Exception =>
      println(s"[Error] Failed to fetch $url: ${e.getMessage}")
      None

def parseRSS(xmlContent: String): Option[List[FeedItem]] =
  try
    val xml = XML.loadString(xmlContent)
    val items: List[FeedItem] = (xml \\ "item").toList.map: node =>
      val title = (node \ "title").text.trim
      val link = (node \ "link").text.trim
      FeedItem(title, link)
    Some(items)
  catch
    case e: Exception =>
      println(s"[Error] Failed to parse XML: ${e.getMessage}")
      None

def formatter(items: List[(String, String)]): String =
  items
    .map((title, link) => s"Title: $title\nLink: $link\n---")
    .mkString("\n")

def saveToFile(path: String, content: String): Option[Unit] =
  try
    Files.write(Paths.get(path), content.getBytes)
    println(s"Saved to $path")
    Some(())
  catch
    case e: Exception =>
      println(s"[Error] Failed to save to $path: ${e.getMessage}")
      None

def processFeed(
    fetch: String => Option[String],
    parse: String => Option[List[FeedItem]],
    formatter: List[(String, String)] => String
)(job: Job): Option[Unit] =
  for
    fetched <- fetch(job.url)
    parsed <- parse(fetched)
    formatted = formatter(parsed.map(item => (item.title, item.link)))
    mayBeSaved <- saveToFile(job.outputPath, formatted)
  yield mayBeSaved

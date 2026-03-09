package level1

import scala.xml.XML
import scala.xml.Elem
import scala.collection.mutable.ListBuffer
import java.nio.file.Files
import java.nio.file.Paths
import scala.io.Source

final class RSSProcess(val feeds: List[(String, String)]):

  def run(): Unit =
    println("🚀 Starting Level 1 Sequential Processor...")

    for (xmlPath, path) <- feeds do
      println(s"  [Start] Processing $xmlPath")
      val source = Source.fromFile(xmlPath)
      val xmlContent =
        try
          source.mkString
        finally
          source.close()

      val xml: Elem = XML.loadString(xmlContent)
      val itemNodes = xml \\ "item"
      val items = new ListBuffer[(String, String)]

      var i = 0
      while i < itemNodes.size do
        val node = itemNodes(i)
        val title = (node \ "title").text.trim
        val link = (node \ "link").text.trim
        items.append((title, link))
        i += 1

      val sb = new StringBuilder
      i = 0
      while i < items.size do
        val item = items(i)
        val title = item._1
        val link = item._2
        sb.append(s"Title: $title\nLink: $link\n---\n")
        i += 1

      val content = sb.toString
      Files.write(Paths.get(path), content.getBytes)
      println(s"  [Done] Saved to $path")

    println("✅ All done (sequentially).")

object Level1Processor:
  val defaultFeeds: List[(String, String)] = List(
    "sample-data/level4/tech.xml" -> "output/tech_news.txt",
    "sample-data/level4/business.xml" -> "output/business_news.txt"
  )

@main def runRSSProcess(): Unit =
  RSSProcess(Level1Processor.defaultFeeds).run()

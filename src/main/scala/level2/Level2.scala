package level2

import scala.xml.XML
import java.nio.file.{Files, Paths}
import scala.io.Source

// A Unary Function: logic to fetch data
def fetchData(xmlPath: String): String = ???

// A Unary Function: logic to parse XML into a List of Strings
def parseRSS(xmlContent: String): List[(String, String)] = ???

def formatter(items: List[(String, String)]): String = ???

def saveToFile(path: String, content: String): Unit = ???

// An Extension Method adding Level 2 power to the feeds list
def processFeed(
    fetch: String => String, // Higher-Order Parameter
    parse: String => List[(String, String)], // Higher-Order P,arameter
    formatter: List[(String, String)] => String // Higher-Order Parameter
    // Save to file
)(xmlPath: String): String = ???

@main def runLevel2(): Unit =
  val bbcFeeds = List(
    "sample-data/level4/tech.xml" -> "output/tech_news.txt",
    "sample-data/level4/business.xml" -> "output/business_news.txt"
  )

  val rssProcessed = processFeed(fetchData, parseRSS, formatter)

  bbcFeeds.foreach:
    (xmlPath, path) =>
      val content: String = rssProcessed(xmlPath)
      // saveToFile(path, content)

package level3.maybe

package level2.reveal

import scala.xml.XML
import java.nio.file.{Files, Paths}
import scala.io.Source

sealed trait Maybe[+A]:
  def map[B](f: A => B): Maybe[B]
  def flatMap[B](f: A => Maybe[B]): Maybe[B]
  def getOrElse[B >: A](default: => B): B
  def fold[B](ifEmpty: => B)(f: A => B): B

case class Just[+A](value: A) extends Maybe[A]:
  def map[B](f: A => B): Maybe[B] = Just(f(value))
  def flatMap[B](f: A => Maybe[B]): Maybe[B] = f(value)
  def getOrElse[B >: A](default: => B): B = value
  def fold[B](ifEmpty: => B)(f: A => B): B = f(value)

case object Empty extends Maybe[Nothing]:
  def map[B](f: Nothing => B): Maybe[B] = Empty
  def flatMap[B](f: Nothing => Maybe[B]): Maybe[B] = Empty
  def getOrElse[B](default: => B): B = default
  def fold[B](ifEmpty: => B)(f: Nothing => B): B = ifEmpty

// A Unary Function: logic to fetch data
def fetchData(xmlPath: String): Maybe[String] =
  try
    val source = Source.fromFile(xmlPath)
    val xmlContent =
      try
        source.mkString
      finally
        source.close()
    Just(xmlContent)
  catch
    case e: Exception =>
      println(s"[Error] Failed to fetch data from $xmlPath: ${e.getMessage}")
      Empty

// A Unary Function: logic to parse XML into a List of Strings
def parseRSS(xmlContent: String): List[(String, String)] =
  val xml = XML.loadString(xmlContent)
  val items: List[(String, String)] = (xml \\ "item").toList.map: node =>
    val title = (node \ "title").text.trim
    val link = (node \ "link").text.trim
    (title, link)
  items

def formatter(items: List[(String, String)]): String =
  items
    .map((title, link) => s"Title: $title\nLink: $link\n---")
    .mkString("\n")

def saveToFile(path: String, content: String): Maybe[Unit] =
  try
    Files.write(Paths.get(path), content.getBytes)
    println(s"✅ Saved to $path")
    Just(())
  catch
    case e: Exception =>
      println(s"[Error] Failed to save to $path: ${e.getMessage}")
      Empty

// Given instances (the “implicit” implementations)
given (String => Maybe[String]) = fetchData
given (String => List[(String, String)]) = parseRSS
given (List[(String, String)] => String) = formatter

// An Extension Method adding Level 2 power to the feeds list
def processFeed(xmlPath: String)(using
    fetch: String => Maybe[String], // Higher-Order Parameter
    parse: String => List[(String, String)], // Higher-Order Parameter
    formatter: List[(String, String)] => String // Higher-Order Parameter
): Maybe[String] = for {
  fetched <- fetch(xmlPath)
  parsed = parse(fetched)
  formatted = formatter(parsed)
} yield formatted

@main def runLevel2OwnMaybe(): Unit =
  val bbcFeeds = List(
    "sample-data/level4/tech.xml" -> "output/tech_news.txt",
    "sample-data/level4/business.xml" -> "output/business_news.txt"
  )

  // val rssProcessed = processFeed(fetchData, parseRSS, formatter)

  bbcFeeds.foreach: (xmlPath, path) =>
    val content = processFeed(xmlPath)
    content.fold(())(c => saveToFile(path, c))

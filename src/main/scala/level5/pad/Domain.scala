package level5.paa
import scala.xml.XML
// ---------------------------------------------------------------------------
// Domain (Core Business Logic)
// ---------------------------------------------------------------------------
// Pure business logic and data structures.
// Completely independent of IO, databases, or frameworks.
case class RemoteRss(url: String)
case class LocalRss(path: String)
case class LocalFileLocation(path: String)

case class RSSItem(title: String, link: String)

sealed trait FeedError
object FeedError:
  case class ReadError(source: String, message: String) extends FeedError
  case class ParseError(raw: String, message: String) extends FeedError
  case class WriteError(target: String, message: String) extends FeedError

//Pure domain logic for parsing and formatting RSS feeds
//Pure domain logic for parsing and formatting RSS feeds
def parseRSS(content: String): Either[FeedError, List[RSSItem]] =
  content.split(",").map(_.trim).toList match
    case title :: link :: Nil if title.nonEmpty && link.nonEmpty =>
      Right(List(RSSItem(title, link)))
    case _ =>
      Left(FeedError.ParseError(content, "Expected format: title, link"))

def format(items: List[RSSItem]): String =
  items
    .map(item => s"Title: ${item.title} Link: ${item.link}\n---")
    .mkString("\n")

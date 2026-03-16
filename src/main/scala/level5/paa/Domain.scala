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

//Pure domain logic for parsing and formatting RSS feeds
def parseRSS(content: String): List[RSSItem] =
  val token = content.split(",")
  List(RSSItem(token.head, token.last))

def format(items: List[RSSItem]): String =
  items
    .map(item => s"Title: ${item.title} Link: ${item.link}\n---")
    .mkString("\n")

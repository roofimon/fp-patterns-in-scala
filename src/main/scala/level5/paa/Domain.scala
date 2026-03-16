package level5.paa
import scala.xml.XML

// ---------------------------------------------------------------------------
// Domain (Core Business Logic)
// ---------------------------------------------------------------------------
// Pure business logic and data structures.
// Completely independent of IO, databases, or frameworks.

final case class LocalPath(value: String)
final case class RemoteUrl(value: String)

case class RSSItem(title: String, link: String)

object RSSLogic:
  def parse(xmlContent: String): List[RSSItem] =
    val xml = XML.loadString(xmlContent)
    (xml \\ "item").toList.map: node =>
      val title = (node \ "title").text.trim
      val link = (node \ "link").text.trim
      RSSItem(title, link)

  def format(items: List[RSSItem]): String =
    items
      .map(item => s"Title: ${item.title}\nLink: ${item.link}\n---")
      .mkString("\n")

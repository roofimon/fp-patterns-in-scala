package level7

import scala.xml.XML
import java.nio.file.{Files, Paths}
import scala.io.Source
import scala.util.Try

// --- 1. THE UNIVERSAL RECURSION ENGINE (Pure Math) ---

// Fix allows us to define a recursive structure without manual recursion
case class Fix[F[_]](unfix: F[Fix[F]])

// The Catamorphism: Collapses a Fix[F] structure into a single value A
def cata[F[_], A](structure: Fix[F])(algebra: F[A] => A)(using fmap: Functor[F]): A =
  algebra(fmap.map(structure.unfix)(next => cata(next)(algebra)))

// Minimal Functor capability needed for the engine
trait Functor[F[_]]:
  def map[A, B](fa: F[A])(f: A => B): F[B]

// --- 2. THE RSS DOMAIN ALGEBRA (The Shape) ---

enum RSSOp[+A] derives CanEqual:
  case Fetch(url: String, next: String => A) extends RSSOp[A]
  case Parse(xml: String, next: List[(String, String)] => A) extends RSSOp[A]
  case Save(path: String, content: String, next: Unit => A) extends RSSOp[A]
  case Done extends RSSOp[Nothing]

// Prove RSSOp is a Functor so the engine can "walk" it
given Functor[RSSOp] with
  def map[A, B](fa: RSSOp[A])(f: A => B): RSSOp[B] = fa match
    case RSSOp.Fetch(u, n)   => RSSOp.Fetch(u, n.andThen(f))
    case RSSOp.Parse(x, n)   => RSSOp.Parse(x, n.andThen(f))
    case RSSOp.Save(p, c, n) => RSSOp.Save(p, c, n.andThen(f))
    case RSSOp.Done          => RSSOp.Done

// --- 3. THE IMPLEMENTATION (The Interpreter) ---

// This algebra defines HOW each step is executed.
// It is 100% decoupled from the "walking" of the list.
def interpreter: RSSOp[Unit] => Unit =
  case RSSOp.Fetch(url, next) =>
    println(s"🌐 Fetching: $url")
    val content = Try(Source.fromURL(url).mkString).getOrElse("<error/>")
    next(content)

  case RSSOp.Parse(xml, next) =>
    println(s"🔍 Parsing XML...")
    val items = Try {
      (XML.loadString(xml) \\ "item").toList.map { node =>
        ((node \ "title").text.trim, (node \ "link").text.trim)
      }
    }.getOrElse(Nil)
    next(items)

  case RSSOp.Save(path, content, next) =>
    Files.write(Paths.get(path), content.getBytes)
    println(s"✅ Saved to: $path")
    next(())

  case RSSOp.Done =>
    println("🏁 Process Finished.")

// --- 4. THE PROGRAM DEFINITION (The Structure) ---

// We define our program as a data structure, not a set of commands.
def rssProgram(url: String, path: String): Fix[RSSOp] =
  Fix(RSSOp.Fetch(url, xml =>
    Fix(RSSOp.Parse(xml, items =>
      Fix(RSSOp.Save(path, items.mkString("\n"), _ =>
        Fix(RSSOp.Done)
      ))
    ))
  ))

@main def runLevel7(): Unit =
  val program = rssProgram("https://feeds.bbci.co.uk/news/world/rss.xml", "world.txt")

  // Running the program using the universal Catamorphism
  cata(program)(interpreter)

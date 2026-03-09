package level1.reveal

import scala.io.Source
import java.nio.file.{Files, Paths}

/** LEVEL 1: Word count refactored into small functions.
  *   1. Pure logic — no I/O, no side effects, easily testable
  *   2. Effects — file reading, printing
  *   3. Orchestration — wires pure + effect functions together
  */

// ── 1. Pure logic ─────────────────────────────────────────────────────────

def normalize(raw: String): String =
  val sb = new StringBuilder
  for c <- raw.toLowerCase do
    if c.isLetter then sb.append(c) else sb.append(' ')
  sb.toString

def toWords(text: String): List[String] =
  text.split("\\s+").filter(_.nonEmpty).toList

def countWords(words: List[String]): Map[String, Int] =
  words.groupBy(identity).view.mapValues(_.size).toMap

def topN(counts: Map[String, Int], n: Int): List[(String, Int)] =
  counts.toList.sortBy(-_._2).take(n)

def formatReport(
    filePath: String,
    total: Int,
    unique: Int,
    top: List[(String, Int)]
): String =
  val sb = new StringBuilder
  sb.append(s"File: $filePath\n")
  sb.append(s"Total words: $total\n")
  sb.append(s"Unique words: $unique\n")
  sb.append("Top 5:\n")
  for (word, n) <- top do sb.append(s"  $word: $n\n")
  sb.toString

// ── 2. Effects ────────────────────────────────────────────────────────────

def readFile(filePath: String): String =
  val source = Source.fromFile(filePath)
  try source.mkString
  finally source.close()

def printReport(report: String): Unit =
  println(report)

// ── 3. Orchestration ──────────────────────────────────────────────────────

def run(filePath: String): Unit =
  println("🚀 Word count (Level 1 — refactored)...")
  val raw = readFile(filePath)
  val words = toWords(normalize(raw))
  val counts = countWords(words)
  val top = topN(counts, 5)
  val report = formatReport(filePath, words.size, words.distinct.size, top)
  printReport(report)
  println("✅ Done.")

@main def runWordCount(): Unit =
  val path = "tech_news.txt"
  if Files.exists(Paths.get(path)) then run(path)
  else
    println(
      s"File not found: $path (run RSS processor first or set another path)"
    )

package foundation.leve3

/** Custom container that behaves like Option (Maybe, Just, Empty). Step demos
  * live in separate files: YourOwnContainerStep1.scala ... Step5.scala.
  */

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

def maybeParseInt(s: String): Maybe[Int] =
  s.toIntOption match
    case Some(n) => Just(n)
    case None    => Empty

def maybeDivide(a: Int, b: Int): Maybe[Int] =
  if b == 0 then Empty else Just(a / b)

@main def runYourOwnContainer(): Unit =
  println("=== Your own container (Maybe, like Option) – full demo ===\n")

  println("1. Construction:")
  println(s"   Just(42) = ${Just(42)}")
  println(s"   Empty = $Empty\n")

  println("2. map:")
  println(s"   Just(21).map(_ * 2) = ${Just(21).map(_ * 2)}")
  println(
    s"   Empty.map((x: Int) => x * 2) = ${Empty.map((x: Int) => x * 2)}\n"
  )

  println("3. flatMap and getOrElse:")
  println(s"   Just(10).flatMap(n => maybeDivide(n, 2)) = ${Just(10)
      .flatMap(n => maybeDivide(n, 2))}")
  println(s"   Just(10).flatMap(n => maybeDivide(n, 0)) = ${Just(10)
      .flatMap(n => maybeDivide(n, 0))}")
  println(s"   Just(7).getOrElse(0) = ${Just(7).getOrElse(0)}")
  println(s"   Empty.getOrElse(99) = ${Empty.getOrElse(99)}\n")

  println("4. for-comprehension (parse two strings and divide):")
  val result = for
    n <- maybeParseInt("20")
    m <- maybeParseInt("4")
    r <- maybeDivide(n, m)
  yield r
  println(s"   maybeParseInt(\"20\"), maybeParseInt(\"4\"), divide => $result")
  val failResult = for
    n <- maybeParseInt("20")
    m <- maybeParseInt("x")
    r <- maybeDivide(n, m)
  yield r
  println(s"   maybeParseInt(\"20\"), maybeParseInt(\"x\") => $failResult\n")

  println("5. fold:")
  println(s"   Just(3).fold(0)(_ * 10) = ${Just(3).fold(0)(_ * 10)}")
  println(s"   Empty.fold(0)(_ => 0) = ${Empty.fold(0)(_ => 0)}")

  println("\nDone.")

package foundation.level3

sealed trait Maybe[+A]
case class Just[+A](value: A) extends Maybe[A]
case object Empty extends Maybe[Nothing]

// sealed trait Maybe[+A]:
//   def map[B](f: A => B): Maybe[B]

// case class Just[+A](value: A) extends Maybe[A]:
//   def map[B](f: A => B): Maybe2[B] = Just2(f(value))

// case object Empty extends Maybe[Nothing]:
//   def map[B](f: Nothing => B): Maybe[B] = Empty

def divideByZero(a: Int, b: Int): Maybe[Int] =
  if b == 0 then Empty else Just(a / b)

def double(a: Int): Int = a * 2

@main def runYourOwnContainerStep1(): Unit =
  println("=== Step 1: The container only (Maybe1, Just1, Empty1) ===\n")
  println(
    "We have two cases: a value (Just1) or nothing (Empty1). No map/flatMap yet."
  )
  println(s"   Just1(42) = ${Just(42)}")
  println(s"   Empty = $Empty")
  println("\nDone.")
  val a1: Maybe[Int] = divideByZero(10, 2)
  val a2: Maybe[Int] = divideByZero(10, 5)

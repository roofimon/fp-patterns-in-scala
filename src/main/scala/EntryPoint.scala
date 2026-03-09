@main def main(): Unit =
  println("Hello, World!")

  val square = (x: Int) => x * x

  def doThing(f: Int => Int, a: Int): Int = f.apply(a)
  val add2 = doThing((a: Int) => a + 2, 1)
  val multiply2 = doThing((a: Int) => a * 2, 3)

  val multiply3 = doThing((a: Int) => a * 2, doThing((a: Int) => a + 2, 1))

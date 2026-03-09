println("Hello, World!")
val x = 5
x

def add(a: Int, b: Int): Int = {
  a + b
}
val result = add(3, 4)

def multiply(x: Int, y: Int): Int = x * y
val product = multiply(6, 7)

val square = (x: Int) => x * x

def doThing(f: Int => Int, a: Int): Int = f.apply(a)
val add2 = doThing((a: Int) => a + 2, 1)
val multiply2 = doThing((a: Int) => a * 2, 3)

val multiply3 = doThing((a: Int) => a * 2, doThing((a: Int) => a + 2, 1))

import scala.annotation.tailrec
import scala.annotation.targetName

def factorial(n: Int): Int =
  if n <= 1 then 1
  else n * factorial(n - 1)

val students1 = List("Alice", "Bob", "Charlie", "David", "Eve")
val scores1 = List(100, 90, 80, 70, 60)

factorial(4)

def addOne(x: Int): Int = x + 1
def double(x: Int): Int = x * 2

// Composing functions using function chaining
def addOneThenDouble(x: Int): Int = double(addOne(x))

// Composing functions using higher-order functions
def compose[A, B, C](f: B => C, g: A => B): A => C =
  x => f(g(x))

// Example usage
val result1 = addOneThenDouble(3) // (3 + 1) * 2 = 8
val result2 = compose(double, addOne)(3)
val result3 = (addOne andThen double)
println(result3(3))

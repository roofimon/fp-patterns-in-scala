println("Hello, World!")
val x = 5
x

def add(a: Int, b: Int): Int = {
  a + b
}

val result = add(3, 4)

def multiply1(x: Int, y: Int): Int = x * y

val product = multiply1(6, 7)

val xx = multiply1(1, add(2, 3))

// f(g(x)) = f.g(x, y)

val square = (x: Int) => x * x

def doThing(f: Int => Int, a: Int): Int = f(a)

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

//add transform2 as extension method to List[String]
extension (scoreList: List[Int])
  def transform2(adder: Int): List[Int] =
    if scoreList.isEmpty then Nil
    else (scoreList.head + adder) :: scoreList.tail.transform2(adder)

scores1.transform2(5)

def curriedAdd(x: Int)(y: Int): Int = x + y
val add5 = curriedAdd(5)
val temp2 = add5

add5(10)

def doSideEffect = println("Doing side effect")

val sideEffectResult = doSideEffect

val temp = sideEffectResult

extension (scoreList: List[Int])
  def transform3(f: (Int, Int) => Int)(value: Int): List[Int] =
    if scoreList.isEmpty then Nil
    else f(scoreList.head, value) :: scoreList.tail.transform3(f)(value)

val multiply: (Int, Int) => Int = (x, y) => x * y
//val multiplyBy = transform3(multiply)

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

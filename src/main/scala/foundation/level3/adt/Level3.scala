package foundation.level3.adt

/// ADTs: Algebraic Data Types
// ----------------------------------
// - ADTs are a way to model data using types that can be combined in various ways.
// - They are "algebraic" because they can be thought of as sums and products of types.
// - They are "data types" because they are used to represent data in a structured way.
// - ADTs are a powerful tool for modeling complex data and behavior in a type-safe way

// Example: Modeling a simple traffic light system using ADTs (Scala 3 enum)
enum TrafficLight derives CanEqual:
  case Red, Yellow, Green

// Example: Modeling a simple shape system using ADTs
sealed trait Shape
case class Circle(radius: Double) extends Shape
case class Rectangle(width: Double, height: Double) extends Shape
case class Triangle(base: Double, height: Double) extends Shape

// Example: Accounting for the possibility of failure using ADTs
sealed trait Result[+A]
case class Success[A](value: A) extends Result[A]
case class Failure(error: String) extends Result[Nothing]

def divide(a: Int, b: Int): Result[Int] =
  if b == 0 then Failure("Division by zero")
  else Success(a / b)

def meaningOf(light: TrafficLight): Unit =
  light match
    case TrafficLight.Red    => println("Stop")
    case TrafficLight.Yellow => println("Slow down")
    case TrafficLight.Green  => println("Go")

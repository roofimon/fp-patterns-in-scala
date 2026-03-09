package foundation.level2

// Composition: Building Complex Behavior from Simple Functions
// ----------------------------------- Composition is the process of combining simple functions to build more complex behavior.
// - In functional programming, we often build complex behavior by composing simpler functions together.
// - This allows us to create reusable, modular code that is easier to reason about and maintain.
// - Composition can be done using higher-order functions, function chaining, or other techniques depending on the context.

def addOne(x: Int): Int = x + 1
def double(x: Int): Int = x * 2

// Composing functions using function chaining
def addOneThenDouble(x: Int): Int = double(addOne(x))

// Composing functions using higher-order functions
def compose[A, B, C](f: B => C, g: A => B): A => C =
  x => f(g(x))

// Example usage
val result1: Int => Int = (addOne andThen double)

// val composedFunction = compose(double, addOne)

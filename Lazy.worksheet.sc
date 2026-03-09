// 1. Strict val (Runs immediately)
val eagerValue = {
  println("🏃 Running Eagerly!")
  10 + 10
}

// 2. Lazy val (Does NOTHING yet)
val lazyValue = {
  println("🐢 Running Lazily...")
  20 + 20
}

println("--- App Started ---")
// Eager value is already computed, so this just prints the result
println(eagerValue)
println(eagerValue)

println(lazyValue) // Evaluation happens HERE for the first time
println(lazyValue) // Result is cached (memoized); println won't run again

import scala.util.Random

// 1. THE ACTION: This function simulates work and returns a value.
def rollDice(): Int = {
  println("🎲 [ACTION] Rolling the dice...")
  Random.nextInt(100)
}

// 2. CALL-BY-VALUE: The "Box"
// The dice is rolled BEFORE the function starts.
// We pass the RESULT (the number) into the function.
def callByValue(x: Int): Unit = {
  println("--- Starting Call-by-Value ---")
  println(s"First use: $x")
  println(s"Second use: $x")
}

// 3. CALL-BY-NAME: The "Envelope"
// The dice is NOT rolled yet.
// We pass the CALCULATION (the recipe) into the function.
def callByName(x: => Int): Unit = {
  println("--- Starting Call-by-Name ---")
  println(s"First use: $x") // The dice rolls NOW
  println(s"Second use: $x") // The dice rolls AGAIN!
}

// --- WORKSHEET EXECUTION ---

// Test 1: Notice how "[ACTION]" appears BEFORE "Starting..."
callByValue(rollDice())

// Test 2: Notice how "[ACTION]" appears AFTER "Starting..."
// and it appears TWICE!
callByName(rollDice())

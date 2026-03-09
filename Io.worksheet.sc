// A simple wrapper that holds a "thunk" (an envelope)
case class Plan[A](run: () => A) {

  // Transform the result inside the envelope
  def map[B](f: A => B): Plan[B] =
    Plan(() => f(run()))

  // Chain two envelopes together
  def flatMap[B](f: A => Plan[B]): Plan[B] =
    Plan(() => f(run()).run())
}

object Plan {
  // The MAGIC: : => A suspends the code immediately
  def record[A](body: => A): Plan[A] =
    Plan(() => body)
}

// A simple recursive retry
def retry[A](action: Plan[A], attempts: Int): Plan[A] =
  Plan(() => {
    try {
      action.run()
    } catch {
      case e if attempts > 1 =>
        println(s"  (Retrying... $attempts attempts left)")
        retry(action, attempts - 1).run()
      case e => throw e
    }
  })

import scala.util.Random

val step1 = Plan.record {
  println("🔋 Step 1: Charging system...")
  if (Random.nextBoolean()) {
    println("✅ Charge Successful!")
    "System Charged"
  } else {
    println("❌ ERROR: Power Surge!")
    throw new RuntimeException("Charging Failed")
  }
}

val step2 = Plan.record {
  println("🚀 Step 2: Launching rocket!")
  "Orbit Reached"
}

// 2. Compose them (STILL NOTHING HAPPENS)
// We are just building a bigger envelope.
val program = for {
  msg1 <- retry(step1, 10)
  msg2 <- step2
} yield s"Final Status: $msg1 -> $msg2"

println("--- Script is written, but the rocket is still on the ground ---")
println("\n--- T-Minus Zero: Running the program NOW ---")

// This is the ONLY moment the side effects actually happen
val result1 = program.run()

println(s"\nResult of the mission: $result1")

//val resilientStep = retry(step1, 10)

//println("\n--- Attempting Resilient Charge ---")

//println(s"Final Status: ${resilientStep.run()}")

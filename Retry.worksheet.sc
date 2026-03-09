import scala.util.Random

// 1. THE MINI-IO ENGINE
case class MiniIO[A](run: () => A) {
  def map[B](f: A => B): MiniIO[B] = MiniIO(() => f(run()))

  def flatMap[B](f: A => MiniIO[B]): MiniIO[B] =
    MiniIO(() => f(run()).run())
}

object MiniIO {
  def delay[A](body: => A): MiniIO[A] = MiniIO(() => body)
}

// 2. THE RESILIENCE LOGIC
def retry[A](action: MiniIO[A], attempts: Int): MiniIO[A] = {
  if (attempts <= 1) action
  else
    MiniIO(() => {
      try {
        action.run()
      } catch {
        case _: Throwable =>
          println(s"  ⚠️  Failed! Retries remaining: ${attempts - 1}")
          retry(action, attempts - 1).run()
      }
    })
}

def retryWithCounter[A](
    action: MiniIO[A],
    maxAttempts: Int,
    currentAttempt: Int = 1
): MiniIO[A] = {
  MiniIO(() => {
    try {
      // If it's not the first time, we tell the user we are retrying
      if (currentAttempt > 1) {
        println(s"🔄 [RETRY #${currentAttempt - 1}] Attempting recovery...")
      }

      action.run() // Try to open the envelope
    } catch {
      case _: Throwable if currentAttempt < maxAttempts =>
        println(s"  ❌ Attempt $currentAttempt failed.")
        // Increment the counter and pass it to the NEXT blueprint
        retryWithCounter(action, maxAttempts, currentAttempt + 1).run()

      case e: Throwable =>
        println(s"  💀 Final Attempt $currentAttempt failed. Giving up.")
        throw e
    }
  })
}

// 3. THE FLAKY STEP (50% Success Rate)
val step1 = MiniIO.delay {
  println("🔋 Charging system...")
  if (Random.nextBoolean()) {
    "✅ SUCCESS"
  } else {
    throw new RuntimeException("💥 Power Surge!!!!")
  }
}

// 4. THE WORKSHEET EXECUTION
println("--- Starting Resilient Mission ---")

// We wrap the flaky step in a 5-attempt retry policy
val finalProgram = retryWithCounter(step1, 8) //retry(flakyStep1, 3)

// Only now does the code actually execute
val finalResult = finalProgram.run()

println(finalResult)

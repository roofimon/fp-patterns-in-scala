package foundation.level4.retry

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import java.nio.file.{Files, Paths}
import scala.concurrent.duration.*
import cats.effect.testkit.TestControl

/*
 * IO Monad: Chef and Fried Egg Recipe Metaphor
 * --------------------------------------------
 * - A RECIPE is like IO[A]: a description of steps to produce a value (e.g. a fried egg).
 *   It is just a description — no cooking happens when we write it.
 * - WRITING THE RECIPE = composing IO with map / flatMap / for-comprehension (pure).
 * - THE CHEF RUNS THE RECIPE = calling unsafeRunSync() (or similar); only then do
 *   side effects (heat pan, crack egg, etc.) happen.
 */

case class FriedEgg(description: String)

// Recipe steps: each returns an IO action. Nothing runs until the chef executes the recipe.

def heatPan: IO[String] =
  IO.println("  2. Heat the pan over medium heat.").as("Pan is hot")

def crackEgg: IO[String] =
  IO.println("  3. Crack the egg into the pan.").as("Egg is in the pan")

def cook: IO[String] =
  IO.println("  4. Cook until the white is set and the yolk is to your liking.")
    .as("Egg is cooking")

def serve: IO[Option[FriedEgg]] =
  IO.println("  5. Slide onto a plate and serve.")
    .as(Some(FriedEgg("One sunny-side-up fried egg")))

def getPan: IO[String] =
  // IO.println("  1. Get a non-stick pan.").as("Pan")
  IO.println("  Getting pan...") *>
    IO.sleep(5.seconds) *>
    IO.pure("Hot Pan")

// Alternative: Simple manual retry tailored for this use case
extension [A](io: IO[A])
  def retryWithBackoff(maxRetries: Int, initialDelay: FiniteDuration): IO[A] =
    io.handleErrorWith { err =>
      if maxRetries > 0 then
        IO.println(
          s"  🔁 Operation failed: ${err.getMessage}. Retrying in $initialDelay. Retries left: $maxRetries"
        ) *>
          IO.sleep(initialDelay) *>
          io.retryWithBackoff(maxRetries - 1, initialDelay * 2)
      else IO.raiseError(err)
    }

// Writing the recipe: we compose IO values. This is pure — no side effects yet.
val recipeForFriedEgg: IO[Option[FriedEgg]] =
  for
    _ <- getPan
      .timeout(100.millis)
      .retryWithBackoff(3, 100.millis)
      .handleErrorWith(_ =>
        IO.println("  ⏱️ getPan failed after 3 attempts, using backup pan.")
          .as("Backup Pan")
      )
    _ <- heatPan
    _ <- crackEgg
    _ <- cook
    egg <- serve
  yield egg

@main def runIOMonadDemo(): Unit =
  println("🔍 Starting Blueprint Inspection...")
  println("The chef is reading the recipe... (building IO is done)")
  recipeForFriedEgg.unsafeRunSync() match {
    case Some(res) => println(s"✅ Test Passed: Plan resulted in '$res'")
    case None      => println(s"❌ Test Failed: Unexpected error")
  }
